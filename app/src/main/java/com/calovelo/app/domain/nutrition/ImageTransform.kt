// app/src/main/java/com/calovelo/app/domain/nutrition/ImageTransform.kt
package com.calovelo.app.domain.nutrition

import com.calovelo.app.domain.model.BoundingBox
import kotlin.math.max
import kotlin.math.min

/**
 * Maps between a source image and the square model input: `modelX = sourceX * scale + offsetX`.
 * Letterboxing gives non-negative offsets (padding); center-cropping gives non-positive offsets
 * (cropped overflow). One formula, one inverse, so detections can be mapped back either way.
 */
data class ImageTransform(
    val sourceWidth: Int,
    val sourceHeight: Int,
    val inputSize: Int,
    val scale: Float,
    val offsetX: Float,
    val offsetY: Float
) {
    /** Converts a box in model-input pixels to coordinates normalized to the source image (clamped to 0..1). */
    fun toSourceNormalized(box: BoundingBox): BoundingBox {
        fun x(v: Float) = ((v - offsetX) / scale).coerceIn(0f, sourceWidth.toFloat()) / sourceWidth
        fun y(v: Float) = ((v - offsetY) / scale).coerceIn(0f, sourceHeight.toFloat()) / sourceHeight
        return BoundingBox(x(box.left), y(box.top), x(box.right), y(box.bottom))
    }

    companion object {
        /** Fit the whole image inside the square, padding the short side. Keeps every pixel of the plate. */
        fun letterbox(sourceWidth: Int, sourceHeight: Int, inputSize: Int): ImageTransform =
            create(sourceWidth, sourceHeight, inputSize) { sx, sy -> min(sx, sy) }

        /** Fill the square, cropping the long side. */
        fun centerCrop(sourceWidth: Int, sourceHeight: Int, inputSize: Int): ImageTransform =
            create(sourceWidth, sourceHeight, inputSize) { sx, sy -> max(sx, sy) }

        private inline fun create(
            sourceWidth: Int,
            sourceHeight: Int,
            inputSize: Int,
            pick: (Float, Float) -> Float
        ): ImageTransform {
            require(sourceWidth > 0 && sourceHeight > 0) { "Image size must be positive: ${sourceWidth}x$sourceHeight" }
            require(inputSize > 0) { "inputSize must be positive but was $inputSize" }
            val scale = pick(inputSize.toFloat() / sourceWidth, inputSize.toFloat() / sourceHeight)
            return ImageTransform(
                sourceWidth = sourceWidth,
                sourceHeight = sourceHeight,
                inputSize = inputSize,
                scale = scale,
                offsetX = (inputSize - sourceWidth * scale) / 2f,
                offsetY = (inputSize - sourceHeight * scale) / 2f
            )
        }
    }
}
