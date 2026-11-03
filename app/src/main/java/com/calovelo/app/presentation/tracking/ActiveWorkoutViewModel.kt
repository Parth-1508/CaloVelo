package com.calovelo.app.presentation.tracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calovelo.app.domain.model.ActivityType
import com.calovelo.app.domain.model.FeelingTag
import com.calovelo.app.domain.model.FinishOutcome
import com.calovelo.app.domain.model.StartResult
import com.calovelo.app.domain.model.WorkoutFeedback
import com.calovelo.app.domain.repository.WorkoutRepository
import com.calovelo.app.domain.usecase.DismissWorkoutFeedbackUseCase
import com.calovelo.app.domain.usecase.FinishWorkoutUseCase
import com.calovelo.app.domain.usecase.PauseWorkoutUseCase
import com.calovelo.app.domain.usecase.ResumeWorkoutUseCase
import com.calovelo.app.domain.usecase.SaveWorkoutFeedbackUseCase
import com.calovelo.app.domain.usecase.StartWorkoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActiveWorkoutViewModel @Inject constructor(
    repository: WorkoutRepository,
    private val startWorkout: StartWorkoutUseCase,
    private val pauseWorkout: PauseWorkoutUseCase,
    private val resumeWorkout: ResumeWorkoutUseCase,
    private val finishWorkout: FinishWorkoutUseCase,
    private val saveWorkoutFeedback: SaveWorkoutFeedbackUseCase,
    private val dismissWorkoutFeedback: DismissWorkoutFeedbackUseCase
) : ViewModel() {

    private val selectedActivity = MutableStateFlow(ActivityType.RUNNING)
    private val isSaving = MutableStateFlow(false)
    private val userMessage = MutableStateFlow<String?>(null)
    private val isFinishing = MutableStateFlow(false)

    private val eventChannel = Channel<ActiveWorkoutEvent>(Channel.BUFFERED)
    val events: Flow<ActiveWorkoutEvent> = eventChannel.receiveAsFlow()

    /**
     * Combines the service-fed tracking state with the pending post-workout feedback. The
     * bottom sheet appears as soon as the repository publishes a [PendingFeedback], whether
     * the workout was finished from this screen or from the notification's Finish action.
     */
    val uiState: StateFlow<ActiveWorkoutUiState> = combine(
        repository.trackingState,
        repository.pendingFeedback,
        selectedActivity,
        isSaving,
        userMessage
    ) { tracking, feedback, selected, saving, message ->
        ActiveWorkoutUiState(
            tracking = tracking,
            selectedActivity = selected,
            feedback = feedback,
            isSaving = saving,
            userMessage = message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ActiveWorkoutUiState()
    )

    fun onActivitySelected(type: ActivityType) {
        if (!uiState.value.tracking.isTracking) selectedActivity.value = type
    }

    fun onStartClicked() {
        when (startWorkout(selectedActivity.value)) {
            StartResult.STARTED, StartResult.ALREADY_RECORDING -> Unit
            StartResult.PERMISSION_MISSING -> userMessage.value =
                "Precise location permission is required to record your route. Enable it in system settings."
            StartResult.LOCATION_SERVICES_OFF -> userMessage.value =
                "Turn on location services (GPS) to record a workout."
            StartResult.FAILED -> userMessage.value =
                "Couldn't start recording. Please try again with the app open."
        }
    }

    fun onLocationPermissionDenied() {
        userMessage.value = "Precise location permission is required to record your route. " +
            "Enable it in system settings."
    }

    fun onPauseClicked() {
        pauseWorkout()
    }

    fun onResumeClicked() {
        resumeWorkout()
    }

    fun onFinishClicked() {
        if (!isFinishing.compareAndSet(expect = false, update = true)) return
        viewModelScope.launch {
            try {
                when (finishWorkout()) {
                    // The RPE sheet appears via pendingFeedback.
                    is FinishOutcome.Saved, FinishOutcome.NotRecording -> Unit
                    FinishOutcome.TooShort -> userMessage.value =
                        "That workout was too short to save."
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                userMessage.value = "Couldn't save your workout. Please try Finish again."
            } finally {
                isFinishing.value = false
            }
        }
    }

    fun onFeedbackSubmitted(rpeScore: Int, feelingTag: FeelingTag?, notes: String) {
        val pending = uiState.value.feedback ?: return
        if (!isSaving.compareAndSet(expect = false, update = true)) return
        viewModelScope.launch {
            try {
                saveWorkoutFeedback(
                    pending.sessionId,
                    WorkoutFeedback(rpeScore = rpeScore, feelingTag = feelingTag, notes = notes)
                )
                eventChannel.send(ActiveWorkoutEvent.NavigateBack)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                userMessage.value = "Couldn't save your feedback. Please try again."
            } finally {
                isSaving.value = false
            }
        }
    }

    /** Skip: the workout is already saved; it simply has no RPE. */
    fun onFeedbackDismissed() {
        if (isSaving.value) return
        dismissWorkoutFeedback()
        viewModelScope.launch { eventChannel.send(ActiveWorkoutEvent.NavigateBack) }
    }

    fun onMessageShown() {
        userMessage.value = null
    }
}
