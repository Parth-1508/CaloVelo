package com.calovelo.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_health_summary")
data class DailyHealthSummaryEntity(
    @PrimaryKey
    @ColumnInfo(name = "date_epoch_day") val dateEpochDay: Long,
    @ColumnInfo(name = "steps") val steps: Long = 0L,
    @ColumnInfo(name = "active_calories_burned") val activeCaloriesBurned: Double = 0.0,
    /** Populated from Health Connect's average BPM for the day (see HealthConnectManager). */
    @ColumnInfo(name = "resting_heart_rate") val restingHeartRate: Int? = null,
    @ColumnInfo(name = "sleep_minutes") val sleepMinutes: Long = 0L,
    @ColumnInfo(name = "water_intake_ml") val waterIntakeMl: Int = 0,
    @ColumnInfo(name = "water_goal_ml") val waterGoalMl: Int = DEFAULT_WATER_GOAL_ML,
    @ColumnInfo(name = "calorie_goal_kcal") val calorieGoalKcal: Int = DEFAULT_CALORIE_GOAL_KCAL,
    @ColumnInfo(name = "last_synced_timestamp") val lastSyncedTimestamp: Long = 0L
) {
    companion object {
        const val DEFAULT_WATER_GOAL_ML = 2_500
        const val DEFAULT_CALORIE_GOAL_KCAL = 2_000
    }
}
