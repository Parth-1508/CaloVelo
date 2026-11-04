// app/src/main/java/com/calovelo/app/domain/gym/StrengthAnalyticsEngine.kt
package com.calovelo.app.domain.gym

import com.calovelo.app.domain.model.GymSet
import com.calovelo.app.domain.model.SetType
import javax.inject.Inject

/**
 * Pure strength maths: no Android, no I/O, no state. Invalid inputs (negative or non-finite weight,
 * fewer than one rep) contribute 0 instead of throwing, so a half-typed set can never crash a live total.
 */
class StrengthAnalyticsEngine @Inject constructor() {

    /** Volume of one set: weight x reps. */
    fun setVolume(weightKg: Double, reps: Int): Double =
        if (isValid(weightKg, reps)) weightKg * reps else 0.0

    fun setVolume(set: GymSet): Double = setVolume(set.weightKg, set.reps)

    /**
     * Total Session Volume = sum of weight x reps over [sets]. Warm-ups count by default (the plain
     * definition); pass `includeWarmups = false` to measure working volume only.
     */
    fun totalSessionVolume(sets: List<GymSet>, includeWarmups: Boolean = true): Double =
        sets.asSequence()
            .filter { includeWarmups || it.type != SetType.WARMUP }
            .sumOf { setVolume(it) }

    /** Volume per exercise id, in first-appearance order. */
    fun volumeByExercise(sets: List<GymSet>, includeWarmups: Boolean = true): Map<String, Double> {
        val result = LinkedHashMap<String, Double>()
        for (set in sets) {
            if (!includeWarmups && set.type == SetType.WARMUP) continue
            result[set.exerciseId] = (result[set.exerciseId] ?: 0.0) + setVolume(set)
        }
        return result
    }

    /**
     * Estimated one-rep max, Epley formula: 1RM = weight x (1 + reps / 30). Returns 0 for an invalid
     * or zero-weight set. Note that Epley applied literally to a single rep gives 1/30 above the
     * lifted weight; accuracy also drops for high-rep sets, so treat it as a trend line.
     */
    fun estimateOneRepMax(weightKg: Double, reps: Int): Double =
        if (isValid(weightKg, reps)) weightKg * (1.0 + reps / EPLEY_DIVISOR) else 0.0

    fun estimateOneRepMax(set: GymSet): Double = estimateOneRepMax(set.weightKg, set.reps)

    /** Highest estimated 1RM among the working (non-warm-up) sets, or null if none qualifies. */
    fun bestEstimatedOneRepMax(sets: List<GymSet>): Double? =
        sets.asSequence()
            .filter { it.type != SetType.WARMUP }
            .map { estimateOneRepMax(it) }
            .filter { it > 0.0 }
            .maxOrNull()

    /**
     * Inverse Epley: the weight expected to be liftable for [reps] reps given a [oneRepMaxKg].
     * Useful for suggesting working weights from a stored max.
     */
    fun weightForReps(oneRepMaxKg: Double, reps: Int): Double =
        if (oneRepMaxKg.isFinite() && oneRepMaxKg > 0.0 && reps >= 1) {
            oneRepMaxKg / (1.0 + reps / EPLEY_DIVISOR)
        } else {
            0.0
        }

    private fun isValid(weightKg: Double, reps: Int): Boolean =
        weightKg.isFinite() && weightKg > 0.0 && reps >= 1

    private companion object {
        const val EPLEY_DIVISOR = 30.0
    }
}
