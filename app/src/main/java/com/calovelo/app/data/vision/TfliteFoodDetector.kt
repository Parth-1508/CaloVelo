// app/src/main/java/com/calovelo/app/data/vision/TfliteFoodDetector.kt
package com.calovelo.app.data.vision

import android.content.Context
import android.graphics.Bitmap
import com.calovelo.app.data.local.dao.FoodCatalogDao
import com.calovelo.app.data.local.mapper.toDomain
import com.calovelo.app.data.local.seed.FoodCatalogSeeder
import com.calovelo.app.domain.model.DetectedFoodCandidate
import com.calovelo.app.domain.model.FoodScanResult
import com.calovelo.app.domain.nutrition.FoodLabelKey
import com.calovelo.app.domain.nutrition.NonMaxSuppression
import com.calovelo.app.domain.nutrition.PortionEstimator
import com.calovelo.app.domain.nutrition.YoloLayout
import com.calovelo.app.domain.nutrition.YoloOutputDecoder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

/**
 * On-device food detector: runs a YOLOv8-style TFLite model against a photo and maps the detected
 * classes to catalog foods. 100% local, no network.
 *
 * ### Graceful fallback
 * The model is optional at build time. If `food_detector.tflite` or `food_labels.txt` is not in
 * `app/src/main/assets/`, or the model is not in a supported format, [detect] returns
 * [FoodScanResult.ModelUnavailable] with a human-readable reason instead of throwing, and the UI
 * lets the user pick from the seeded catalog.
 *
 * ### Expected model
 * * input: `[1, S, S, 3]` float32, RGB, 0..1 (S is read from the model; 640 for the stock export)
 * * output: `[1, 4 + numClasses, anchors]` or `[1, anchors, 4 + numClasses]` float32 (YOLOv8 head,
 *   no objectness), where `numClasses` equals the number of lines in `food_labels.txt`
 * * Ultralytics: `yolo export model=best.pt format=tflite imgsz=640` (FP32 or FP16 weights both work)
 */
@Singleton
class TfliteFoodDetector @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preprocessor: BitmapPreprocessor,
    private val catalogDao: FoodCatalogDao,
    private val seeder: FoodCatalogSeeder
) {

    private sealed interface ModelState {
        class Ready(
            val interpreter: Interpreter,
            val labels: List<String>,
            val inputSize: Int,
            val layout: YoloLayout,
            val numAnchors: Int
        ) : ModelState {
            val outputElementCount: Int get() = (4 + labels.size) * numAnchors
        }

        class Unavailable(val reason: String) : ModelState
    }

    private val loadMutex = Mutex()
    private val inferenceMutex = Mutex() // Interpreter is not thread-safe.

    @Volatile
    private var modelState: ModelState? = null

    /** Null when the model is loaded and usable, otherwise why it is not. Loads lazily, once. */
    suspend fun unavailableReason(): String? = (ensureModel() as? ModelState.Unavailable)?.reason

    /**
     * Detects foods in an upright [bitmap]. Box coordinates in the result are normalized to this
     * bitmap. The bitmap is not modified or recycled.
     */
    suspend fun detect(bitmap: Bitmap): FoodScanResult = withContext(Dispatchers.Default) {
        val ready = when (val state = ensureModel()) {
            is ModelState.Unavailable -> return@withContext FoodScanResult.ModelUnavailable(state.reason)
            is ModelState.Ready -> state
        }
        try {
            val prepared = preprocessor.preprocess(bitmap, ready.inputSize, ResizeMode.LETTERBOX)
            val output = FloatArray(ready.outputElementCount)
            inferenceMutex.withLock { runInference(ready, prepared.buffer, output) }

            val raw = YoloOutputDecoder.decode(
                output = output,
                numAnchors = ready.numAnchors,
                numClasses = ready.labels.size,
                layout = ready.layout,
                inputSize = ready.inputSize,
                confidenceThreshold = CONFIDENCE_THRESHOLD
            )
            val kept = NonMaxSuppression.apply(raw, IOU_THRESHOLD, MAX_DETECTIONS)
            if (kept.isEmpty()) return@withContext FoodScanResult.NothingDetected()

            seeder.ensureSeeded()
            val keys = kept.map { FoodLabelKey.fromLabel(ready.labels[it.classId]) }
            val foodsByKey = catalogDao.getByLabelKeys(keys.distinct()).associateBy { it.labelKey }

            val unmapped = LinkedHashSet<String>()
            val candidates = kept.mapIndexedNotNull { index, detection ->
                val food = foodsByKey[keys[index]]
                if (food == null) {
                    unmapped += ready.labels[detection.classId]
                    null
                } else {
                    val box = prepared.transform.toSourceNormalized(detection.box)
                    DetectedFoodCandidate(
                        food = food.toDomain(),
                        confidence = detection.score,
                        box = box,
                        portionMultiplier = PortionEstimator.multiplier(box.area)
                    )
                }
            }

            if (candidates.isEmpty()) {
                FoodScanResult.NothingDetected(unmapped.toList())
            } else {
                FoodScanResult.Detected(candidates, bitmap.width, bitmap.height)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            FoodScanResult.Failure("Food detection failed: ${e.localizedMessage ?: e.javaClass.simpleName}")
        }
    }

    /** Frees the native interpreter. The singleton normally lives as long as the process. */
    fun close() {
        (modelState as? ModelState.Ready)?.interpreter?.close()
        modelState = null
    }

    private fun runInference(ready: ModelState.Ready, input: ByteBuffer, out: FloatArray) {
        val outputBuffer = ByteBuffer.allocateDirect(out.size * Float.SIZE_BYTES).order(ByteOrder.nativeOrder())
        input.rewind()
        ready.interpreter.run(input, outputBuffer)
        outputBuffer.rewind()
        outputBuffer.asFloatBuffer().get(out)
    }

    private suspend fun ensureModel(): ModelState {
        modelState?.let { return it }
        return loadMutex.withLock {
            modelState ?: withContext(Dispatchers.IO) { loadModel() }.also { modelState = it }
        }
    }

    private fun loadModel(): ModelState {
        val assetNames = context.assets.list("")?.toSet().orEmpty()
        val missing = listOf(MODEL_ASSET, LABELS_ASSET).filter { it !in assetNames }
        if (missing.isNotEmpty()) {
            return ModelState.Unavailable(
                "Food detector not installed (missing ${missing.joinToString(" and ")} in app/src/main/assets). " +
                    "Search the food catalog instead."
            )
        }

        return try {
            val labels = context.assets.open(LABELS_ASSET).bufferedReader().useLines { lines ->
                lines.map { it.trim() }.filter { it.isNotEmpty() }.toList()
            }
            if (labels.isEmpty()) return ModelState.Unavailable("$LABELS_ASSET is empty.")

            val interpreter = Interpreter(
                FileUtil.loadMappedFile(context, MODEL_ASSET),
                Interpreter.Options().setNumThreads(min(MAX_THREADS, Runtime.getRuntime().availableProcessors()))
            )
            validate(interpreter, labels)
        } catch (e: Exception) {
            ModelState.Unavailable("Couldn't load $MODEL_ASSET: ${e.localizedMessage ?: e.javaClass.simpleName}")
        } catch (e: LinkageError) {
            // Native TFLite library missing for this CPU architecture.
            ModelState.Unavailable("TensorFlow Lite is not available on this device: ${e.localizedMessage}")
        }
    }

    /** Returns Ready, or closes the interpreter and returns Unavailable with the reason. */
    private fun validate(interpreter: Interpreter, labels: List<String>): ModelState {
        fun reject(reason: String): ModelState {
            interpreter.close()
            return ModelState.Unavailable(reason)
        }

        val input = interpreter.getInputTensor(0)
        val inShape = input.shape()
        if (input.dataType() != DataType.FLOAT32) {
            return reject("Model input must be float32 (found ${input.dataType()}). Re-export without int8 quantization.")
        }
        if (inShape.size != 4 || inShape[0] != 1 || inShape[3] != 3 || inShape[1] != inShape[2]) {
            return reject("Model input must be [1, S, S, 3] (found ${inShape.contentToString()}).")
        }

        val output = interpreter.getOutputTensor(0)
        val outShape = output.shape()
        if (output.dataType() != DataType.FLOAT32) {
            return reject("Model output must be float32 (found ${output.dataType()}).")
        }
        if (outShape.size != 3 || outShape[0] != 1) {
            return reject("Model output must be [1, 4+classes, anchors] (found ${outShape.contentToString()}).")
        }

        val channels = 4 + labels.size
        return when {
            outShape[1] == channels && outShape[2] != channels ->
                ModelState.Ready(interpreter, labels, inShape[1], YoloLayout.CHANNELS_FIRST, outShape[2])
            outShape[2] == channels ->
                ModelState.Ready(interpreter, labels, inShape[1], YoloLayout.CHANNELS_LAST, outShape[1])
            else -> reject(
                "Model output ${outShape.contentToString()} doesn't match ${labels.size} labels in $LABELS_ASSET " +
                    "(expected a dimension of ${channels}). Make sure the labels file lists every class, one per line."
            )
        }
    }

    companion object {
        const val MODEL_ASSET = "food_detector.tflite"
        const val LABELS_ASSET = "food_labels.txt"

        private const val CONFIDENCE_THRESHOLD = 0.25f
        private const val IOU_THRESHOLD = 0.45f
        private const val MAX_DETECTIONS = 20
        private const val MAX_THREADS = 4
    }
}
