package com.calovelo.app.domain.model

data class DailyHealthSummary(
    val epochDay: Long,
    val steps: Long,
    val activeCaloriesBurned: Double,
    val restingHeartRate: Int?,
    val sleepMinutes: Long,
    val waterIntakeMl: Int,
    val waterGoalMl: Int,
    val calorieGoalKcal: Int,
    val lastSyncedTimestamp: Long
) {
    /** 0f..1f, clamped for progress indicators. */
    val hydrationProgress: Float
        get() = if (waterGoalMl <= 0) 0f else (waterIntakeMl.toFloat() / waterGoalMl).coerceIn(0f, 1f)
}
