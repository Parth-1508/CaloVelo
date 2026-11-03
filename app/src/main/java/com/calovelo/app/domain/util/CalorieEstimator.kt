package com.calovelo.app.domain.util

import com.calovelo.app.domain.model.ActivityType
import kotlin.math.max

/**
 * Active-calorie estimate from MET values: active kcal = (MET - 1) * weight(kg) * hours.
 * Subtracting 1 MET removes resting metabolic rate so the figure is "active" burn.
 *
 * MET-vs-speed tables are interpolated from the Compendium of Physical Activities
 * (Ainsworth et al.); they are estimates, not measurements.
 */
object CalorieEstimator {

    const val DEFAULT_BODY_WEIGHT_KG = 70.0

    // (speed km/h, MET)
    private val running = listOf(
        0.0 to 1.0, 5.0 to 3.5, 6.4 to 6.0, 8.0 to 8.3, 8.4 to 9.0, 9.7 to 9.8, 10.8 to 10.5,
        11.3 to 11.0, 12.1 to 11.5, 12.9 to 11.8, 13.8 to 12.3, 14.5 to 12.8, 16.1 to 14.5,
        17.5 to 16.0, 19.3 to 19.0
    )

    private val walking = listOf(
        0.0 to 1.0, 2.7 to 2.3, 3.2 to 2.8, 4.0 to 3.0, 4.8 to 3.5, 5.6 to 4.3, 6.4 to 5.0,
        7.2 to 7.0, 8.0 to 8.3
    )

    private val cycling = listOf(
        0.0 to 1.0, 8.0 to 3.0, 12.0 to 4.0, 17.6 to 6.8, 20.9 to 8.0, 24.0 to 10.0,
        28.2 to 12.0, 32.2 to 15.8
    )

    fun met(activityType: ActivityType, speedKmh: Double): Double {
        val table = when (activityType) {
            ActivityType.RUNNING -> running
            ActivityType.WALKING -> walking
            ActivityType.OUTDOOR_CYCLING -> cycling
        }
        return interpolate(table, speedKmh.coerceAtLeast(0.0))
    }

    fun activeKilocalories(
        activityType: ActivityType,
        speedKmh: Double,
        weightKg: Double,
        durationSeconds: Double
    ): Double {
        val activeMet = max(0.0, met(activityType, speedKmh) - 1.0)
        return activeMet * weightKg * (durationSeconds / 3600.0)
    }

    private fun interpolate(table: List<Pair<Double, Double>>, x: Double): Double {
        if (x <= table.first().first) return table.first().second
        if (x >= table.last().first) return table.last().second
        val upper = table.indexOfFirst { it.first >= x }
        val (x1, y1) = table[upper]
        val (x0, y0) = table[upper - 1]
        return y0 + (y1 - y0) * (x - x0) / (x1 - x0)
    }
}
