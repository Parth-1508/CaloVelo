// app/src/main/java/com/calovelo/app/data/local/dao/GymWorkoutDao.kt
package com.calovelo.app.data.local.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.calovelo.app.data.local.entity.ExerciseSetEntity
import com.calovelo.app.data.local.entity.GymWorkoutEntity
import kotlinx.coroutines.flow.Flow

/** A finished session with its set count and total volume (sum of weight x reps). */
data class GymSessionAggregateRow(
    @ColumnInfo(name = "session_id") val sessionId: String,
    @ColumnInfo(name = "epoch_day") val epochDay: Long,
    @ColumnInfo(name = "start_time") val startTime: Long,
    @ColumnInfo(name = "end_time") val endTime: Long?,
    @ColumnInfo(name = "notes") val notes: String?,
    @ColumnInfo(name = "set_count") val setCount: Int,
    @ColumnInfo(name = "volume_kg") val volumeKg: Double
)

/** Total volume of all finished sessions that started on [epochDay]. */
data class DailyVolumeRow(
    @ColumnInfo(name = "epoch_day") val epochDay: Long,
    @ColumnInfo(name = "volume_kg") val volumeKg: Double
)

@Dao
interface GymWorkoutDao {

    // ---- Workouts -----------------------------------------------------------------------------

    @Insert
    suspend fun insertWorkout(workout: GymWorkoutEntity)

    @Query("SELECT * FROM gym_workout WHERE session_id = :sessionId LIMIT 1")
    suspend fun getById(sessionId: String): GymWorkoutEntity?

    /** The session in progress (end_time is null), if any. */
    @Query("SELECT * FROM gym_workout WHERE end_time IS NULL ORDER BY start_time DESC LIMIT 1")
    suspend fun getActive(): GymWorkoutEntity?

    @Query("SELECT * FROM gym_workout WHERE end_time IS NULL ORDER BY start_time DESC LIMIT 1")
    fun observeActive(): Flow<GymWorkoutEntity?>

    /** Stamps the end time and notes on a session that is still open. Returns rows changed (0 or 1). */
    @Query("UPDATE gym_workout SET end_time = :endTime, notes = :notes WHERE session_id = :sessionId AND end_time IS NULL")
    suspend fun finishWorkout(sessionId: String, endTime: Long, notes: String?): Int

    /** Deletes the session; its sets go with it (ON DELETE CASCADE). */
    @Query("DELETE FROM gym_workout WHERE session_id = :sessionId")
    suspend fun deleteWorkout(sessionId: String): Int

    // ---- Sets ---------------------------------------------------------------------------------

    @Insert
    suspend fun insertSet(set: ExerciseSetEntity)

    @Query("SELECT * FROM exercise_set WHERE set_id = :setId LIMIT 1")
    suspend fun getSet(setId: String): ExerciseSetEntity?

    /** Sets of a session in the order they were logged (rows are only ever appended or updated in place). */
    @Query("SELECT * FROM exercise_set WHERE workout_id = :workoutId ORDER BY rowid ASC")
    fun observeSets(workoutId: String): Flow<List<ExerciseSetEntity>>

    @Query("SELECT * FROM exercise_set WHERE workout_id = :workoutId ORDER BY rowid ASC")
    suspend fun getSets(workoutId: String): List<ExerciseSetEntity>

    @Query("SELECT MAX(set_index) FROM exercise_set WHERE workout_id = :workoutId AND exercise_id = :exerciseId")
    suspend fun getMaxSetIndex(workoutId: String, exerciseId: String): Int?

    @Query("SELECT COUNT(*) FROM exercise_set WHERE workout_id = :workoutId")
    suspend fun countSets(workoutId: String): Int

    @Query(
        "UPDATE exercise_set SET weight_kg = :weightKg, reps = :reps, rpe = :rpe, set_type = :setType " +
            "WHERE set_id = :setId"
    )
    suspend fun updateSetValues(setId: String, weightKg: Double, reps: Int, rpe: Double?, setType: String): Int

    @Query("DELETE FROM exercise_set WHERE set_id = :setId")
    suspend fun deleteSet(setId: String): Int

    /** Closes the gap left by a deleted set so set_index stays 1..n for the exercise. */
    @Query(
        "UPDATE exercise_set SET set_index = set_index - 1 " +
            "WHERE workout_id = :workoutId AND exercise_id = :exerciseId AND set_index > :deletedIndex"
    )
    suspend fun closeSetIndexGap(workoutId: String, exerciseId: String, deletedIndex: Int): Int

    // ---- History and volume aggregates --------------------------------------------------------

    /** Finished sessions, newest first, each with its set count and total volume. */
    @Query(
        "SELECT w.session_id AS session_id, w.epoch_day AS epoch_day, w.start_time AS start_time, " +
            "w.end_time AS end_time, w.notes AS notes, " +
            "COUNT(s.set_id) AS set_count, COALESCE(SUM(s.weight_kg * s.reps), 0.0) AS volume_kg " +
            "FROM gym_workout w LEFT JOIN exercise_set s ON s.workout_id = w.session_id " +
            "WHERE w.end_time IS NOT NULL " +
            "GROUP BY w.session_id " +
            "ORDER BY w.start_time DESC " +
            "LIMIT :limit"
    )
    fun observeFinishedSessions(limit: Int): Flow<List<GymSessionAggregateRow>>

    /** Daily training volume from [fromEpochDay] on, oldest first; days without a session are absent. */
    @Query(
        "SELECT w.epoch_day AS epoch_day, COALESCE(SUM(s.weight_kg * s.reps), 0.0) AS volume_kg " +
            "FROM gym_workout w INNER JOIN exercise_set s ON s.workout_id = w.session_id " +
            "WHERE w.end_time IS NOT NULL AND w.epoch_day >= :fromEpochDay " +
            "GROUP BY w.epoch_day " +
            "ORDER BY w.epoch_day ASC"
    )
    fun observeDailyVolume(fromEpochDay: Long): Flow<List<DailyVolumeRow>>

    /** Running volume of one session; always emits one row (0 when the session has no sets). */
    @Query("SELECT COALESCE(SUM(weight_kg * reps), 0.0) FROM exercise_set WHERE workout_id = :workoutId")
    fun observeSessionVolume(workoutId: String): Flow<Double>

    /** Every set of [exerciseId] from finished sessions, newest session first; feeds 1RM trends. */
    @Query(
        "SELECT s.* FROM exercise_set s INNER JOIN gym_workout w ON w.session_id = s.workout_id " +
            "WHERE s.exercise_id = :exerciseId AND w.end_time IS NOT NULL " +
            "ORDER BY w.start_time DESC, s.set_index ASC " +
            "LIMIT :limit"
    )
    fun observeExerciseHistory(exerciseId: String, limit: Int): Flow<List<ExerciseSetEntity>>
}
