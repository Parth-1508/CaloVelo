// app/src/main/java/com/calovelo/app/domain/nutrition/YoloOutputDecoder.kt
package com.calovelo.app.domain.nutrition

import com.calovelo.app.domain.model.BoundingBox
import kotlin.math.max

/** Memory layout of the flattened YOLOv8 output tensor. */
enum class YoloLayout {
    /** `[1, 4 + numClasses, numAnchors]` - what the Ultralytics TFLite export produces. */
    CHANNELS_FIRST,

    /** `[1, numAnchors, 4 + numClasses]` - produced by some converters. */
    CHANNELS_LAST
}

/**
 * Decodes a YOLOv8 detection head: per anchor `cx, cy, w, h` followed by one sigmoid score per
 * class (YOLOv8 has no separate objectness channel). Pure Kotlin so it is unit-testable.
 */
object YoloOutputDecoder {

    private const val NORMALIZED_COORDINATE_LIMIT = 1.5f

    private class Candidate(
        val cx: Float,
        val cy: Float,
        val w: Float,
        val h: Float,
        val score: Float,
        val classId: Int
    )

    /**
     * @param coordinatesNormalized `true` if box values are 0..1 fractions of the input, `false` if
     * they are pixels, `null` to detect (no kept coordinate above 1.5 means normalized). The
     * Ultralytics TFLite export is normalized; ONNX-style exports are in pixels.
     * @return boxes in model-input pixels (xyxy, clamped to the input), best [maxCandidates] by
     * score, **before** NMS.
     */
    fun decode(
        output: FloatArray,
        numAnchors: Int,
        numClasses: Int,
        layout: YoloLayout,
        inputSize: Int,
        confidenceThreshold: Float = 0.25f,
        coordinatesNormalized: Boolean? = null,
        maxCandidates: Int = 300
    ): List<RawDetection> {
        require(numAnchors > 0) { "numAnchors must be positive but was $numAnchors" }
        require(numClasses > 0) { "numClasses must be positive but was $numClasses" }
        require(inputSize > 0) { "inputSize must be positive but was $inputSize" }
        val channels = 4 + numClasses
        require(output.size == channels * numAnchors) {
            "Output has ${output.size} values but $channels channels x $numAnchors anchors = ${channels * numAnchors}"
        }

        fun value(channel: Int, anchor: Int): Float = when (layout) {
            YoloLayout.CHANNELS_FIRST -> output[channel * numAnchors + anchor]
            YoloLayout.CHANNELS_LAST -> output[anchor * channels + channel]
        }

        val candidates = ArrayList<Candidate>()
        var largestCoordinate = 0f
        for (anchor in 0 until numAnchors) {
            var bestClass = 0
            var bestScore = Float.NEGATIVE_INFINITY
            for (c in 0 until numClasses) {
                val score = value(4 + c, anchor)
                if (score > bestScore) {
                    bestScore = score
                    bestClass = c
                }
            }
            if (bestScore < confidenceThreshold) continue

            val cx = value(0, anchor)
            val cy = value(1, anchor)
            val w = value(2, anchor)
            val h = value(3, anchor)
            if (!(w > 0f && h > 0f)) continue // also rejects NaN
            largestCoordinate = max(largestCoordinate, max(max(cx, cy), max(w, h)))
            candidates += Candidate(cx, cy, w, h, bestScore, bestClass)
        }

        val normalized = coordinatesNormalized ?: (largestCoordinate <= NORMALIZED_COORDINATE_LIMIT)
        val factor = if (normalized) inputSize.toFloat() else 1f
        val limit = inputSize.toFloat()

        return candidates
            .sortedByDescending { it.score }
            .take(maxCandidates)
            .map { c ->
                RawDetection(
                    box = BoundingBox(
                        left = ((c.cx - c.w / 2f) * factor).coerceIn(0f, limit),
                        top = ((c.cy - c.h / 2f) * factor).coerceIn(0f, limit),
                        right = ((c.cx + c.w / 2f) * factor).coerceIn(0f, limit),
                        bottom = ((c.cy + c.h / 2f) * factor).coerceIn(0f, limit)
                    ),
                    score = c.score,
                    classId = c.classId
                )
            }
    }
}
