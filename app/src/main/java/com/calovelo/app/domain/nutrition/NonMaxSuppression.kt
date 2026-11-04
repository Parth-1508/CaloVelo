// app/src/main/java/com/calovelo/app/domain/nutrition/NonMaxSuppression.kt
package com.calovelo.app.domain.nutrition

import com.calovelo.app.domain.model.BoundingBox

/** A detector output before label mapping. [box] is in whatever pixel/normalized space the producer uses. */
data class RawDetection(
    val box: BoundingBox,
    val score: Float,
    val classId: Int
)

object NonMaxSuppression {

    /**
     * Greedy NMS: visit detections by descending score and keep one unless it overlaps an
     * already-kept box by more than [iouThreshold]. With [classAware] only boxes of the same class
     * suppress each other, so a bowl of dal next to a roti keeps both.
     */
    fun apply(
        detections: List<RawDetection>,
        iouThreshold: Float = 0.45f,
        maxDetections: Int = 20,
        classAware: Boolean = true
    ): List<RawDetection> {
        require(iouThreshold in 0f..1f) { "iouThreshold must be within 0..1 but was $iouThreshold" }
        require(maxDetections > 0) { "maxDetections must be positive but was $maxDetections" }

        val kept = ArrayList<RawDetection>()
        for (candidate in detections.sortedByDescending { it.score }) {
            if (kept.size >= maxDetections) break
            val suppressed = kept.any { winner ->
                (!classAware || winner.classId == candidate.classId) &&
                    winner.box.iou(candidate.box) > iouThreshold
            }
            if (!suppressed) kept += candidate
        }
        return kept
    }
}
