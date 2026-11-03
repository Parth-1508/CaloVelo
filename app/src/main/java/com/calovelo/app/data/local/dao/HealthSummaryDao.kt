package com.calovelo.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.calovelo.app.data.local.entity.DailyHealthSummaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class HealthSummaryDao {

    @Query("SELECT * FROM daily_health_summary WHERE date_epoch_day = :epochDay")
    abstract fun observeByDay(epochDay: Long): Flow<DailyHealthSummaryEntity?>

    @Query("SELECT * FROM daily_health_summary WHERE date_epoch_day = :epochDay")
    abstract suspend fun getByDay(epochDay: Long): DailyHealthSummaryEntity?

    @Query(
        "SELECT * FROM daily_health_summary " +
            "WHERE date_epoch_day BETWEEN :fromEpochDay AND :toEpochDay " +
            "ORDER BY date_epoch_day ASC"
    )
    abstract fun observeRange(fromEpochDay: Long, toEpochDay: Long): Flow<List<DailyHealthSummaryEntity>>

    @Query(
        "SELECT * FROM daily_health_summary WHERE date_epoch_day < :epochDay " +
            "ORDER BY date_epoch_day DESC LIMIT 1"
    )
    abstract suspend fun getMostRecentBefore(epochDay: Long): DailyHealthSummaryEntity?

    @Upsert
    abstract suspend fun upsert(entity: DailyHealthSummaryEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertIfAbsent(entity: DailyHealthSummaryEntity): Long

    @Query(
        "UPDATE daily_health_summary SET " +
            "steps = :steps, " +
            "active_calories_burned = :activeCalories, " +
            "resting_heart_rate = :restingHeartRate, " +
            "sleep_minutes = :sleepMinutes, " +
            "last_synced_timestamp = :syncedAt " +
            "WHERE date_epoch_day = :epochDay"
    )
    abstract suspend fun updateWearableStats(
        epochDay: Long,
        steps: Long,
        activeCalories: Double,
        restingHeartRate: Int?,
        sleepMinutes: Long,
        syncedAt: Long
    )

    @Query("UPDATE daily_health_summary SET water_intake_ml = :intakeMl WHERE date_epoch_day = :epochDay")
    abstract suspend fun updateWaterIntake(epochDay: Long, intakeMl: Int)

    /**
     * Returns the row for [epochDay], creating it first when absent. New rows inherit the
     * most recent goals so a midnight rollover never resets the user's targets.
     */
    @Transaction
    open suspend fun getOrCreate(epochDay: Long): DailyHealthSummaryEntity {
        getByDay(epochDay)?.let { return it }
        val previous = getMostRecentBefore(epochDay)
        val seed = DailyHealthSummaryEntity(
            dateEpochDay = epochDay,
            waterGoalMl = previous?.waterGoalMl ?: DailyHealthSummaryEntity.DEFAULT_WATER_GOAL_ML,
            calorieGoalKcal = previous?.calorieGoalKcal ?: DailyHealthSummaryEntity.DEFAULT_CALORIE_GOAL_KCAL
        )
        insertIfAbsent(seed)
        return getByDay(epochDay) ?: seed
    }

    /** Writes only wearable columns so water intake and goals are never clobbered by a sync. */
    @Transaction
    open suspend fun applyWearableStats(
        epochDay: Long,
        steps: Long,
        activeCalories: Double,
        restingHeartRate: Int?,
        sleepMinutes: Long,
        syncedAt: Long
    ) {
        getOrCreate(epochDay)
        updateWearableStats(epochDay, steps, activeCalories, restingHeartRate, sleepMinutes, syncedAt)
    }
}
