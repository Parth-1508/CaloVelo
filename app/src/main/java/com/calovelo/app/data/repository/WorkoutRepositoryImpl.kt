package com.calovelo.app.data.repository

import android.os.SystemClock
import com.calovelo.app.data.local.dao.WorkoutSessionDao
import com.calovelo.app.data.local.mapper.toDomain
import com.calovelo.app.data.local.mapper.toEntity
import com.calovelo.app.domain.model.ActivityType
import com.calovelo.app.domain.model.FinishOutcome
import com.calovelo.app.domain.model.LiveTrackingState
import com.calovelo.app.domain.model.LocationFix
import com.calovelo.app.domain.model.PendingFeedback
import com.calovelo.app.domain.model.RpeScale
import com.calovelo.app.domain.model.WorkoutFeedback
import com.calovelo.app.domain.model.WorkoutSession
import com.calovelo.app.domain.repository.WorkoutRepository
import com.calovelo.app.domain.tracking.ActivityRecorder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Process-wide singleton that doubles as the "state bus" between the foreground service,
 * the notification and the UI: every consumer observes [trackingState].
 */
@Singleton
class WorkoutRepositoryImpl @Inject constructor(
    private val workoutDao: WorkoutSessionDao
) : WorkoutRepository {

    private val recorder = ActivityRecorder()
    private val finishMutex = Mutex()

    private val _trackingState = MutableStateFlow(LiveTrackingState())
    override val trackingState: StateFlow<LiveTrackingState> = _trackingState.asStateFlow()

    private val _pendingFeedback = MutableStateFlow<PendingFeedback?>(null)
    override val pendingFeedback: StateFlow<PendingFeedback?> = _pendingFeedback.asStateFlow()

    override fun observeWorkouts(): Flow<List<WorkoutSession>> =
        workoutDao.getAllWorkoutsFlow().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getWorkout(id: String): WorkoutSession? =
        workoutDao.getWorkoutById(id)?.toDomain()

    override fun startRecording(activityType: ActivityType): Boolean {
        val started = recorder.start(
            type = activityType,
            wallClockMillis = System.currentTimeMillis(),
            monotonicMillis = SystemClock.elapsedRealtime()
        )
        if (started) {
            _pendingFeedback.value = null
            publishState()
        }
        return started
    }

    override fun pauseRecording(): Boolean {
        val changed = recorder.pause(SystemClock.elapsedRealtime())
        if (changed) publishState()
        return changed
    }

    override fun resumeRecording(): Boolean {
        val changed = recorder.resume(SystemClock.elapsedRealtime())
        if (changed) publishState()
        return changed
    }

    override fun recordLocation(fix: LocationFix) {
        if (recorder.onLocation(fix)) publishState()
    }

    override fun recordTick(monotonicMillis: Long) {
        recorder.onTick(monotonicMillis)
        publishState()
    }

    override suspend fun finishRecording(): FinishOutcome = finishMutex.withLock {
        val recorded = recorder.buildResult(
            wallClockMillis = System.currentTimeMillis(),
            monotonicMillis = SystemClock.elapsedRealtime()
        ) ?: return@withLock FinishOutcome.NotRecording

        if (recorded.durationSeconds < WorkoutRepository.MIN_SAVE_DURATION_SECONDS) {
            recorder.reset()
            publishState()
            return@withLock FinishOutcome.TooShort
        }

        // Persist first: if this throws, the recorder is untouched and the user can retry.
        val entity = recorded.toEntity()
        workoutDao.upsert(entity)

        _pendingFeedback.value = PendingFeedback(
            sessionId = entity.id,
            activityType = recorded.activityType,
            durationSeconds = recorded.durationSeconds,
            distanceMeters = recorded.distanceMeters
        )
        recorder.reset()
        publishState()
        FinishOutcome.Saved(entity.id)
    }

    override fun discardRecording() {
        recorder.reset()
        publishState()
    }

    override suspend fun saveFeedback(sessionId: String, feedback: WorkoutFeedback) {
        require(feedback.rpeScore in RpeScale.MIN..RpeScale.MAX) {
            "RPE must be between ${RpeScale.MIN} and ${RpeScale.MAX}."
        }
        val notes = feedback.notes.trim()
            .take(WorkoutRepository.MAX_NOTES_LENGTH)
            .ifEmpty { null }

        val updatedRows = workoutDao.updateFeedback(
            id = sessionId,
            rpeScore = feedback.rpeScore,
            feelingTag = feedback.feelingTag?.name,
            userNotes = notes
        )
        check(updatedRows > 0) { "Workout $sessionId no longer exists." }

        if (_pendingFeedback.value?.sessionId == sessionId) _pendingFeedback.value = null
    }

    override fun dismissFeedback() {
        _pendingFeedback.value = null
    }

    private fun publishState() {
        _trackingState.value = recorder.snapshot()
    }
}
