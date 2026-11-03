package com.calovelo.app.domain.model

/**
 * Wearable aggregates for one local calendar day.
 * [averageHeartRateBpm] is the day's average BPM (null when no samples exist).
 */
data class HealthStats(
    val epochDay: Long,
    val steps: Long,
    val activeCaloriesKcal: Double,
    val averageHeartRateBpm: Int?,
    val sleepMinutes: Long
)
