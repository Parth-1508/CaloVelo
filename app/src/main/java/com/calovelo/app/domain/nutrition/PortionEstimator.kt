// app/src/main/java/com/calovelo/app/domain/nutrition/PortionEstimator.kt
package com.calovelo.app.domain.nutrition

import kotlin.math.roundToInt

/**
 * Rough portion estimate from how much of the frame a detected food covers. A single-camera
 * photo has no depth, so this is a starting point the user fine-tunes with the gram slider.
 */
object PortionEstimator {
    /** A typical serving fills about a quarter of a plate-level photo. */
    const val REFERENCE_AREA_FRACTION = 0.25f
    const val MIN_MULTIPLIER = 0.5f
    const val MAX_MULTIPLIER = 2.0f
    const val STEP = 0.25f

    /**
     * @param boxAreaFraction detected box area as a fraction of the whole image (0..1).
     * @return portion multiplier relative to the catalog's default serving, clamped to
     * [MIN_MULTIPLIER]..[MAX_MULTIPLIER] and snapped to [STEP]. Invalid input yields 1.0.
     */
    fun multiplier(boxAreaFraction: Float): Float {
        if (!(boxAreaFraction > 0f)) return 1f // also catches NaN
        val clamped = (boxAreaFraction / REFERENCE_AREA_FRACTION).coerceIn(MIN_MULTIPLIER, MAX_MULTIPLIER)
        return (clamped / STEP).roundToInt() * STEP
    }
}
