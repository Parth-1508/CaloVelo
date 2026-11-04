// app/src/main/java/com/calovelo/app/domain/repository/GymRepository.kt
package com.calovelo.app.domain.repository

import com.calovelo.app.domain.model.ActiveGymSession
import com.calovelo.app.domain.model.DailyGymVolume
import com.calovelo.app.domain.model.Exercise
import com.calovelo.app.domain.model.FinishGymResult
import com.calovelo.app.domain.model.GymLimits
import com.calovelo.app.domain.model.GymSession
import com.calovelo.app.domain.model.GymSessionSummary
import com.calovelo.app.domain.model.GymSet
import com.calovelo.app.domain.model.LogSetResult
import com.calovelo.app.domain.model.MuscleGroup
import com.calovelo.app.domain.model.SetType
import kotlinx.coroutines.flow.Flow

interface GymRepository {

    /** The session in progress with all its sets, or null when none; re-emits on every change. */
    fun observeActiveSession(): Flow<ActiveGymSession?>

    /** Finished sessions, newest first, with set count and total volume. */
    fun observeHistory(limit: Int): Flow<List<GymSessionSummary>>

    /** Total volume per day for finished sessions from [fromEpochDay] on, oldest first. */
    fun observeDailyVolume(fromEpochDay: Long): Flow<List<DailyGymVolume>>

    /** Finished-session sets of one exercise, newest session first (input for 1RM trends). */
    fun observeExerciseHistory(exerciseId: String, limit: Int): Flow<List<GymSet>>

    /** Starts a session, or returns the one already in progress: there is never more than one. */
    suspend fun startSession(): GymSession

    /** Appends a set to an active session. Input is validated by the use case before it gets here. */
    suspend fun logSet(
        sessionId: String,
        exerciseId: String,
        weightKg: Double,
        reps: Int,
        rpe: Double?,
        type: SetType
    ): LogSetResult

    /** Edits a set of an active session. Returns false if it is gone or its session has finished. */
    suspend fun updateSet(setId: String, weightKg: Double, reps: Int, rpe: Double?, type: SetType): Boolean

    /** Deletes a set of an active session and renumbers the exercise's remaining sets. */
    suspend fun deleteSet(setId: String): Boolean

    /** Ends the session. A session without sets is deleted instead of being saved to history. */
    suspend fun finishSession(sessionId: String, notes: String?): FinishGymResult

    /** Deletes the session and all its sets without saving anything to history. */
    suspend fun discardSession(sessionId: String)

    /** Blank [query] with a null [muscleGroup] returns the catalog alphabetically. */
    suspend fun searchExercises(
        query: String,
        muscleGroup: MuscleGroup?,
        limit: Int = GymLimits.DEFAULT_SEARCH_LIMIT
    ): List<Exercise>

    suspend fun getExercises(ids: Collection<String>): List<Exercise>
}
