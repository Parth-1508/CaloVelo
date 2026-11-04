// app/src/main/java/com/calovelo/app/presentation/training/GymTrackerUiState.kt
package com.calovelo.app.presentation.training

import com.calovelo.app.domain.model.EquipmentType
import com.calovelo.app.domain.model.MuscleGroup
import com.calovelo.app.domain.model.SetType

/** The session in progress. The UI derives the elapsed clock from [startTimeMillis]. */
data class GymSessionUi(
    val sessionId: String,
    val epochDay: Long,
    val startTimeMillis: Long
)

data class GymSetUi(
    val setId: String,
    /** 1-based position among this exercise's sets. */
    val setIndex: Int,
    val weightKg: Double,
    val reps: Int,
    val rpe: Double?,
    val setType: SetType,
    val volumeKg: Double,
    /** Epley estimate for this set; 0 when the set has no load. */
    val estimatedOneRepMaxKg: Double
)

/** One exercise of the current session with the sets logged for it so far. */
data class GymExerciseUi(
    val exerciseId: String,
    val name: String,
    val muscleGroup: MuscleGroup,
    val equipmentType: EquipmentType,
    val sets: List<GymSetUi>,
    val volumeKg: Double,
    /** Best estimated 1RM among working (non-warm-up) sets; null until one qualifies. */
    val bestEstimatedOneRepMaxKg: Double?
)

/** A catalog entry in the exercise picker. */
data class ExerciseSearchResultUi(
    val exerciseId: String,
    val name: String,
    val muscleGroup: MuscleGroup,
    val equipmentType: EquipmentType,
    /** True when the exercise is already part of the active session. */
    val isInSession: Boolean
)

data class GymHistoryItemUi(
    val sessionId: String,
    val epochDay: Long,
    val startTimeMillis: Long,
    val durationMinutes: Int,
    val setCount: Int,
    val volumeKg: Double,
    val notes: String?
)

data class GymTrackerUiState(
    val isLoading: Boolean = true,
    /** Null when no session is in progress. */
    val session: GymSessionUi? = null,
    /** Exercises of the active session in the order the user added them. */
    val exercises: List<GymExerciseUi> = emptyList(),
    /** Live total of weight x reps over every set logged in the active session. */
    val totalVolumeKg: Double = 0.0,
    val totalSets: Int = 0,
    val searchQuery: String = "",
    val selectedMuscleGroup: MuscleGroup? = null,
    val searchResults: List<ExerciseSearchResultUi> = emptyList(),
    val isSearching: Boolean = false,
    /** Finished sessions, newest first. */
    val history: List<GymHistoryItemUi> = emptyList(),
    /** True while a start / finish / discard call is in flight; disable those buttons meanwhile. */
    val isBusy: Boolean = false,
    val userMessage: String? = null
) {
    val hasActiveSession: Boolean get() = session != null
}
