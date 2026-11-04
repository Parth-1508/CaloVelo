// app/src/main/java/com/calovelo/app/presentation/training/GymTrackerViewModel.kt
package com.calovelo.app.presentation.training

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calovelo.app.domain.gym.StrengthAnalyticsEngine
import com.calovelo.app.domain.model.ActiveGymSession
import com.calovelo.app.domain.model.EditSetResult
import com.calovelo.app.domain.model.EquipmentType
import com.calovelo.app.domain.model.Exercise
import com.calovelo.app.domain.model.FinishGymResult
import com.calovelo.app.domain.model.GymSessionSummary
import com.calovelo.app.domain.model.LogSetResult
import com.calovelo.app.domain.model.MuscleGroup
import com.calovelo.app.domain.model.SetType
import com.calovelo.app.domain.usecase.DeleteGymSetUseCase
import com.calovelo.app.domain.usecase.DiscardGymSessionUseCase
import com.calovelo.app.domain.usecase.FinishGymSessionUseCase
import com.calovelo.app.domain.usecase.GetExercisesByIdsUseCase
import com.calovelo.app.domain.usecase.LogGymSetUseCase
import com.calovelo.app.domain.usecase.ObserveActiveGymSessionUseCase
import com.calovelo.app.domain.usecase.ObserveGymHistoryUseCase
import com.calovelo.app.domain.usecase.SearchExercisesUseCase
import com.calovelo.app.domain.usecase.StartGymSessionUseCase
import com.calovelo.app.domain.usecase.UpdateGymSetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class GymTrackerViewModel @Inject constructor(
    observeActiveSession: ObserveActiveGymSessionUseCase,
    observeHistory: ObserveGymHistoryUseCase,
    private val startSession: StartGymSessionUseCase,
    private val logSet: LogGymSetUseCase,
    private val updateSet: UpdateGymSetUseCase,
    private val deleteSet: DeleteGymSetUseCase,
    private val finishSession: FinishGymSessionUseCase,
    private val discardSession: DiscardGymSessionUseCase,
    private val searchExercises: SearchExercisesUseCase,
    private val getExercisesByIds: GetExercisesByIdsUseCase,
    private val analytics: StrengthAnalyticsEngine,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val muscleFilter = MutableStateFlow<MuscleGroup?>(null)
    private val isBusy = MutableStateFlow(false)
    private val userMessage = MutableStateFlow<String?>(null)
    private val isSearching = MutableStateFlow(false)

    /**
     * Exercises the user added to the active session, in the order added. An exercise has no row in
     * the database until its first set is logged, so the picks live in [SavedStateHandle] (they survive
     * process death) and are tied to a session id so a stale list can never leak into a new session.
     */
    private val pendingExerciseIds: StateFlow<String> =
        savedStateHandle.getStateFlow(KEY_PENDING_IDS, "")
    private val pendingSessionId: StateFlow<String> =
        savedStateHandle.getStateFlow(KEY_PENDING_SESSION, "")

    private val activeSession: Flow<ActiveGymSession?> = observeActiveSession()

    /** Everything about the active session, resolved to display data. */
    private val sessionPart: Flow<SessionPart> = combine(
        activeSession,
        pendingSessionId,
        pendingExerciseIds
    ) { active, pendingSession, pendingIds ->
        val addedIds = if (active != null && pendingSession == active.session.sessionId) {
            pendingIds.split(ID_SEPARATOR).filter { it.isNotEmpty() }
        } else {
            emptyList()
        }
        active to addedIds
    }.mapLatest { (active, addedIds) ->
        if (active == null) {
            SessionPart.NONE
        } else {
            val orderedIds = (addedIds + active.sets.map { it.exerciseId }).distinct()
            val catalog = getExercisesByIds(orderedIds)
            SessionPart(active = active, exercises = buildExercises(active, orderedIds, catalog))
        }
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private val searchResults: Flow<List<Exercise>> = combine(searchQuery, muscleFilter) { q, g -> q to g }
        .debounce(SEARCH_DEBOUNCE_MS)
        .distinctUntilChanged()
        .onEach { isSearching.value = true }
        .mapLatest { (query, group) ->
            // isSearching is cleared only when a search finishes. A cancelled (superseded) search leaves
            // it true because the newer one has already set it and will clear it.
            try {
                searchExercises(query, group).also { isSearching.value = false }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                isSearching.value = false
                userMessage.value = "Couldn't search exercises. Please try again."
                emptyList()
            }
        }
        // Emit immediately so the session and history are not held back by the debounce or by the
        // one-off catalog seeding that the first search triggers.
        .onStart { emit(emptyList()) }

    private val historyItems: Flow<List<GymHistoryItemUi>> =
        observeHistory().map { list -> list.map { it.toUi() } }

    val uiState: StateFlow<GymTrackerUiState> = combine(
        sessionPart,
        searchResults,
        historyItems,
        combine(searchQuery, muscleFilter, isSearching) { q, g, s -> Triple(q, g, s) },
        combine(isBusy, userMessage) { busy, message -> busy to message }
    ) { session, results, history, search, transient ->
        val inSession = session.exercises.mapTo(HashSet()) { it.exerciseId }
        GymTrackerUiState(
            isLoading = false,
            session = session.active?.session?.let {
                GymSessionUi(it.sessionId, it.epochDay, it.startTimeMillis)
            },
            exercises = session.exercises,
            totalVolumeKg = session.active?.let { analytics.totalSessionVolume(it.sets) } ?: 0.0,
            totalSets = session.active?.sets?.size ?: 0,
            searchQuery = search.first,
            selectedMuscleGroup = search.second,
            searchResults = results.map { it.toResultUi(isInSession = it.id in inSession) },
            isSearching = search.third,
            history = history,
            isBusy = transient.first,
            userMessage = transient.second
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GymTrackerUiState()
    )

    // ---- Session -------------------------------------------------------------------------------

    fun onStartSession() {
        if (!isBusy.compareAndSet(expect = false, update = true)) return
        viewModelScope.launch {
            try {
                startSession()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                userMessage.value = "Couldn't start the workout. Please try again."
            } finally {
                isBusy.value = false
            }
        }
    }

    fun onFinishSession(notes: String? = null) {
        val sessionId = uiState.value.session?.sessionId ?: return
        if (!isBusy.compareAndSet(expect = false, update = true)) return
        viewModelScope.launch {
            try {
                userMessage.value = when (val result = finishSession(sessionId, notes)) {
                    is FinishGymResult.Finished ->
                        "Workout saved: ${result.summary.setCount} sets, ${result.summary.totalVolumeKg.roundToInt()} kg total volume."
                    FinishGymResult.DiscardedEmpty -> "Nothing was logged, so the workout wasn't saved."
                    FinishGymResult.NoActiveSession -> "That workout was already finished."
                }
                clearPending()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                userMessage.value = "Couldn't finish the workout. Please try again."
            } finally {
                isBusy.value = false
            }
        }
    }

    fun onDiscardSession() {
        val sessionId = uiState.value.session?.sessionId ?: return
        if (!isBusy.compareAndSet(expect = false, update = true)) return
        viewModelScope.launch {
            try {
                discardSession(sessionId)
                clearPending()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                userMessage.value = "Couldn't discard the workout. Please try again."
            } finally {
                isBusy.value = false
            }
        }
    }

    // ---- Exercises -----------------------------------------------------------------------------

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query.take(MAX_QUERY_LENGTH)
    }

    fun onMuscleGroupSelected(group: MuscleGroup?) {
        muscleFilter.value = group
    }

    /** Adds an exercise to the active session; adding one that is already there is a no-op. */
    fun onAddExercise(exerciseId: String) {
        val sessionId = uiState.value.session?.sessionId ?: return
        val current = currentPendingIds(sessionId)
        if (exerciseId in current || uiState.value.exercises.any { it.exerciseId == exerciseId }) return
        savePending(sessionId, current + exerciseId)
    }

    /** Removes an exercise that has no sets yet. Exercises with logged sets are removed by deleting their sets. */
    fun onRemoveExercise(exerciseId: String) {
        val state = uiState.value
        val sessionId = state.session?.sessionId ?: return
        if (state.exercises.firstOrNull { it.exerciseId == exerciseId }?.sets?.isNotEmpty() == true) return
        savePending(sessionId, currentPendingIds(sessionId) - exerciseId)
    }

    // ---- Sets ----------------------------------------------------------------------------------

    fun onLogSet(exerciseId: String, weightKg: Double, reps: Int, rpe: Double? = null, type: SetType = SetType.NORMAL) {
        val sessionId = uiState.value.session?.sessionId ?: run {
            userMessage.value = "Start a workout before logging sets."
            return
        }
        runCatchingMessage("Couldn't log that set.") {
            when (val result = logSet(sessionId, exerciseId, weightKg, reps, rpe, type)) {
                is LogSetResult.Logged -> Unit
                LogSetResult.NoActiveSession -> userMessage.value = "That workout has already finished."
                LogSetResult.UnknownExercise -> userMessage.value = "That exercise isn't in the catalog."
                is LogSetResult.Invalid -> userMessage.value = result.reason
            }
        }
    }

    fun onUpdateSet(setId: String, weightKg: Double, reps: Int, rpe: Double?, type: SetType) =
        runCatchingMessage("Couldn't update that set.") {
            when (val result = updateSet(setId, weightKg, reps, rpe, type)) {
                EditSetResult.Updated -> Unit
                EditSetResult.NotFound -> userMessage.value = "That set no longer exists."
                is EditSetResult.Invalid -> userMessage.value = result.reason
            }
        }

    fun onDeleteSet(setId: String) = runCatchingMessage("Couldn't delete that set.") {
        if (!deleteSet(setId)) userMessage.value = "That set no longer exists."
    }

    fun onMessageShown() {
        userMessage.value = null
    }

    // ---- Helpers -------------------------------------------------------------------------------

    private fun currentPendingIds(sessionId: String): List<String> =
        if (savedStateHandle.get<String>(KEY_PENDING_SESSION) == sessionId) {
            savedStateHandle.get<String>(KEY_PENDING_IDS).orEmpty().split(ID_SEPARATOR).filter { it.isNotEmpty() }
        } else {
            emptyList()
        }

    private fun savePending(sessionId: String, ids: List<String>) {
        savedStateHandle[KEY_PENDING_SESSION] = sessionId
        savedStateHandle[KEY_PENDING_IDS] = ids.distinct().joinToString(ID_SEPARATOR)
    }

    private fun clearPending() {
        savedStateHandle[KEY_PENDING_SESSION] = ""
        savedStateHandle[KEY_PENDING_IDS] = ""
    }

    private fun buildExercises(
        active: ActiveGymSession,
        orderedIds: List<String>,
        catalog: Map<String, Exercise>
    ): List<GymExerciseUi> = orderedIds.map { id ->
        val exercise = catalog[id]
        val sets = active.sets.filter { it.exerciseId == id }
        GymExerciseUi(
            exerciseId = id,
            name = exercise?.name ?: id.replace('_', ' '),
            muscleGroup = exercise?.muscleGroup ?: MuscleGroup.OTHER,
            equipmentType = exercise?.equipmentType ?: EquipmentType.OTHER,
            sets = sets.map { set ->
                GymSetUi(
                    setId = set.setId,
                    setIndex = set.setIndex,
                    weightKg = set.weightKg,
                    reps = set.reps,
                    rpe = set.rpe,
                    setType = set.type,
                    volumeKg = analytics.setVolume(set),
                    estimatedOneRepMaxKg = analytics.estimateOneRepMax(set)
                )
            },
            volumeKg = analytics.totalSessionVolume(sets),
            bestEstimatedOneRepMaxKg = analytics.bestEstimatedOneRepMax(sets)
        )
    }

    private fun runCatchingMessage(failure: String, block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                userMessage.value = failure
            }
        }
    }

    private data class SessionPart(
        val active: ActiveGymSession?,
        val exercises: List<GymExerciseUi>
    ) {
        companion object {
            val NONE = SessionPart(active = null, exercises = emptyList())
        }
    }

    private companion object {
        const val KEY_PENDING_SESSION = "gym_pending_session_id"
        const val KEY_PENDING_IDS = "gym_pending_exercise_ids"
        const val ID_SEPARATOR = ","
        const val SEARCH_DEBOUNCE_MS = 250L
        const val MAX_QUERY_LENGTH = 80
    }
}

private fun Exercise.toResultUi(isInSession: Boolean) = ExerciseSearchResultUi(
    exerciseId = id,
    name = name,
    muscleGroup = muscleGroup,
    equipmentType = equipmentType,
    isInSession = isInSession
)

private fun GymSessionSummary.toUi() = GymHistoryItemUi(
    sessionId = session.sessionId,
    epochDay = session.epochDay,
    startTimeMillis = session.startTimeMillis,
    durationMinutes = (durationMillis / 60_000L).toInt(),
    setCount = setCount,
    volumeKg = totalVolumeKg,
    notes = session.notes
)
