package com.calovelo.app.domain.repository

import com.calovelo.app.domain.model.ActivityType
import com.calovelo.app.domain.model.FinishOutcome
import com.calovelo.app.domain.model.LiveTrackingState
import com.calovelo.app.domain.model.LocationFix
import com.calovelo.app.domain.model.PendingFeedback
import com.calovelo.app.domain.model.WorkoutFeedback
import com.calovelo.app.domain.model.WorkoutSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Single owner of the active recording. The foreground service feeds it measurements
 * ([recordLocation], [recordTick]); the UI and the notification observe [trackingState].
 */
interface WorkoutRepository {

    val trackingState: StateFlow<LiveTrackingState>

    /** Non-null while a just-finished workout is waiting for RPE feedback. */
    val pendingFeedback: StateFlow<PendingFeedback?>

    fun observeWorkouts(): Flow<List<WorkoutSession>>

    suspend fun getWorkout(id: String): WorkoutSession?

    fun startRecording(activityType: ActivityType): Boolean

    fun pauseRecording(): Boolean

    fun resumeRecording(): Boolean

    fun recordLocation(fix: LocationFix)

    fun recordTick(monotonicMillis: Long)

    /**
     * Persists the finished workout (route polyline included) to Room, then stops recording.
     * If the database write fails the exception propagates and the recording stays active so
     * the user can retry without losing data.
     */
    suspend fun finishRecording(): FinishOutcome

    /** Drops the in-memory recording without saving. */
    fun discardRecording()

    suspend fun saveFeedback(sessionId: String, feedback: WorkoutFeedback)

    /** Skips feedback; the workout stays saved without an RPE score. */
    fun dismissFeedback()

    companion object {
        /** Recordings shorter than this are discarded as accidental starts. */
        const val MIN_SAVE_DURATION_SECONDS = 10L
        const val MAX_NOTES_LENGTH = 500
    }
}
