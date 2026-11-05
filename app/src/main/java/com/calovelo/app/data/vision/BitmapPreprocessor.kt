// app/src/main/java/com/calovelo/app/data/vision/BitmapPreprocessor.kt
package com.calovelo.app.data.vision

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.media.ExifInterface
import com.calovelo.app.domain.nutrition.ImageTransform
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

/** The model-ready tensor plus the transform needed to map detections back onto the source photo. */
class PreprocessedImage(
    /** Float32, NHWC, RGB, values in 0..1, rewound and ready to hand to the interpreter. */
    val buffer: ByteBuffer,
    val transform: ImageTransform
)

enum class ResizeMode {
    /** Keep the whole photo, pad the short side with gray (YOLO's default). */
    LETTERBOX,

    /** Fill the square, cropping the long side. */
    CENTER_CROP
}

enum class InputLayout {
    /** Float32, NHWC, [1, S, S, 3], interleaved RGB */
    NHWC,

    /** Float32, NCHW, [1, 3, S, S], planar R, then G, then B (PyTorch export default) */
    NCHW
}

/**
 * Turns a photo into the 640x640 Float32 tensor the detector expects:
 * decode (downsampled) -> apply EXIF orientation -> letterbox / center-crop -> normalize to [0..1].
 */
@Singleton
class BitmapPreprocessor @Inject constructor() {

    /**
     * Decodes an image so that its longest side is at most [maxSide] pixels and rotates/flips it per
     * its EXIF orientation, so what comes back is upright regardless of how the phone was held.
     * [open] is called once per pass (bounds, EXIF, pixels), so it must return a fresh stream each time.
     * @return null if the data is not a decodable image or cannot be read.
     */
    fun decodeUpright(open: () -> InputStream, maxSide: Int = DEFAULT_MAX_DECODE_SIDE): Bitmap? {
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            open().use { BitmapFactory.decodeStream(it, null, bounds) }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

            val orientation = open().use { stream ->
                ExifInterface(stream).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            }

            val options = BitmapFactory.Options().apply {
                inSampleSize = sampleSizeFor(max(bounds.outWidth, bounds.outHeight), maxSide)
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val decoded = open().use { BitmapFactory.decodeStream(it, null, options) } ?: return null
            applyExifOrientation(decoded, orientation)
        } catch (e: IOException) {
            null
        } catch (e: OutOfMemoryError) {
            null
        }
    }

    /**
     * Resizes [source] into a square [inputSize] x [inputSize] tensor. The source bitmap is not
     * modified or recycled.
     */
    fun preprocess(
        source: Bitmap,
        inputSize: Int = DEFAULT_INPUT_SIZE,
        mode: ResizeMode = ResizeMode.LETTERBOX,
        inputLayout: InputLayout = InputLayout.NHWC
    ): PreprocessedImage {
        val transform = when (mode) {
            ResizeMode.LETTERBOX -> ImageTransform.letterbox(source.width, source.height, inputSize)
            ResizeMode.CENTER_CROP -> ImageTransform.centerCrop(source.width, source.height, inputSize)
        }

        val square = Bitmap.createBitmap(inputSize, inputSize, Bitmap.Config.ARGB_8888)
        try {
            val canvas = Canvas(square)
            canvas.drawColor(PADDING_COLOR)
            // Same destination rectangle for both modes: padding gives offsets >= 0, cropping gives
            // offsets <= 0 and the canvas clips the overflow.
            val destination = RectF(
                transform.offsetX,
                transform.offsetY,
                transform.offsetX + source.width * transform.scale,
                transform.offsetY + source.height * transform.scale
            )
            canvas.drawBitmap(source, null, destination, Paint(Paint.FILTER_BITMAP_FLAG))
            return PreprocessedImage(toNormalizedBuffer(square, inputLayout), transform)
        } finally {
            square.recycle()
        }
    }

    private fun toNormalizedBuffer(square: Bitmap, layout: InputLayout = InputLayout.NHWC): ByteBuffer {
        val size = square.width
        val pixels = IntArray(size * size)
        square.getPixels(pixels, 0, size, 0, 0, size, size)

        val buffer = ByteBuffer.allocateDirect(size * size * CHANNELS * BYTES_PER_FLOAT)
            .order(ByteOrder.nativeOrder())
        when (layout) {
            InputLayout.NHWC -> {
                for (pixel in pixels) {
                    buffer.putFloat(Color.red(pixel) * INV_255)
                    buffer.putFloat(Color.green(pixel) * INV_255)
                    buffer.putFloat(Color.blue(pixel) * INV_255)
                }
            }
            InputLayout.NCHW -> {
                for (pixel in pixels) {
                    buffer.putFloat(Color.red(pixel) * INV_255)
                }
                for (pixel in pixels) {
                    buffer.putFloat(Color.green(pixel) * INV_255)
                }
                for (pixel in pixels) {
                    buffer.putFloat(Color.blue(pixel) * INV_255)
                }
            }
        }
        buffer.rewind()
        return buffer
    }

    private fun applyExifOrientation(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
                matrix.setRotate(180f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.setRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.setRotate(-90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(-90f)
            else -> return bitmap // normal / undefined
        }
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (rotated !== bitmap) bitmap.recycle()
        return rotated
    }

    /** Largest power of two that keeps the decoded long side at or above [maxSide]. */
    private fun sampleSizeFor(longSide: Int, maxSide: Int): Int {
        var sample = 1
        while (longSide / (sample * 2) >= maxSide) sample *= 2
        return sample
    }

    companion object {
        const val DEFAULT_INPUT_SIZE = 640
        const val DEFAULT_MAX_DECODE_SIDE = 1_600

        /** Ultralytics pads letterboxes with gray 114. */
        private val PADDING_COLOR = Color.rgb(114, 114, 114)
        private const val CHANNELS = 3
        private const val BYTES_PER_FLOAT = 4
        private const val INV_255 = 1f / 255f
    }
}
