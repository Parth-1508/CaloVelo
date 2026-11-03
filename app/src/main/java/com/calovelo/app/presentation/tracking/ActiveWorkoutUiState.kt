package com.calovelo.app.presentation.tracking

import com.calovelo.app.domain.model.ActivityType
import com.calovelo.app.domain.model.LiveTrackingState
import com.calovelo.app.domain.model.PendingFeedback

data class ActiveWorkoutUiState(
    val tracking: LiveTrackingState = LiveTrackingState(),
    /** Activity chosen in the selector; ignored while a workout is recording. */
    val selectedActivity: ActivityType = ActivityType.RUNNING,
    /** Non-null while the RPE bottom sheet should be shown. */
    val feedback: PendingFeedback? = null,
    val isSaving: Boolean = false,
    val userMessage: String? = null
) {
    /** The activity to display: the live one while recording, otherwise the selection. */
    val displayedActivity: ActivityType
        get() = if (tracking.isTracking) tracking.activityType else selectedActivity
}

sealed interface ActiveWorkoutEvent {
    /** Emitted after feedback was saved or skipped; the screen should return to the dashboard. */
    data object NavigateBack : ActiveWorkoutEvent
}
