package com.calovelo.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.calovelo.app.data.local.entity.WorkoutSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSessionDao {

    @Upsert
    suspend fun upsert(session: WorkoutSessionEntity)

    @Delete
    suspend fun delete(session: WorkoutSessionEntity)

    @Query("DELETE FROM workout_session WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM workout_session ORDER BY start_time DESC")
    fun getAllWorkoutsFlow(): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_session WHERE id = :id")
    suspend fun getWorkoutById(id: String): WorkoutSessionEntity?

    /** @return number of rows updated (0 when the session does not exist). */
    @Query(
        "UPDATE workout_session SET rpe_score = :rpeScore, feeling_tag = :feelingTag, " +
            "user_notes = :userNotes WHERE id = :id"
    )
    suspend fun updateFeedback(id: String, rpeScore: Int, feelingTag: String?, userNotes: String?): Int
}
