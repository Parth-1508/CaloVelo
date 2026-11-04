// app/src/main/java/com/calovelo/app/domain/usecase/GymUseCases.kt
package com.calovelo.app.domain.usecase

import com.calovelo.app.domain.model.ActiveGymSession
import com.calovelo.app.domain.model.DailyGymVolume
import com.calovelo.app.domain.model.EditSetResult
import com.calovelo.app.domain.model.Exercise
import com.calovelo.app.domain.model.FinishGymResult
import com.calovelo.app.domain.model.GymLimits
import com.calovelo.app.domain.model.GymSession
import com.calovelo.app.domain.model.GymSessionSummary
import com.calovelo.app.domain.model.GymSet
import com.calovelo.app.domain.model.LogSetResult
import com.calovelo.app.domain.model.MuscleGroup
import com.calovelo.app.domain.model.SetType
import com.calovelo.app.domain.repository.GymRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveActiveGymSessionUseCase @Inject constructor(
    private val repository: GymRepository
) {
    operator fun invoke(): Flow<ActiveGymSession?> = repository.observeActiveSession()
}

class StartGymSessionUseCase @Inject constructor(
    private val repository: GymRepository
) {
    /** Starts a session, or returns the one already in progress. */
    suspend operator fun invoke(): GymSession = repository.startSession()
}

class LogGymSetUseCase @Inject constructor(
    private val repository: GymRepository
) {
    suspend operator fun invoke(
        sessionId: String,
        exerciseId: String,
        weightKg: Double,
        reps: Int,
        rpe: Double? = null,
        type: SetType = SetType.NORMAL
    ): LogSetResult {
        GymLimits.validateSet(weightKg, reps, rpe)?.let { return LogSetResult.Invalid(it) }
        return repository.logSet(sessionId, exerciseId, weightKg, reps, rpe, type)
    }
}

class UpdateGymSetUseCase @Inject constructor(
    private val repository: GymRepository
) {
    suspend operator fun invoke(
        setId: String,
        weightKg: Double,
        reps: Int,
        rpe: Double?,
        type: SetType
    ): EditSetResult {
        GymLimits.validateSet(weightKg, reps, rpe)?.let { return EditSetResult.Invalid(it) }
        return if (repository.updateSet(setId, weightKg, reps, rpe, type)) {
            EditSetResult.Updated
        } else {
            EditSetResult.NotFound
        }
    }
}

class DeleteGymSetUseCase @Inject constructor(
    private val repository: GymRepository
) {
    suspend operator fun invoke(setId: String): Boolean = repository.deleteSet(setId)
}

class FinishGymSessionUseCase @Inject constructor(
    private val repository: GymRepository
) {
    suspend operator fun invoke(sessionId: String, notes: String? = null): FinishGymResult =
        repository.finishSession(sessionId, notes?.trim()?.take(GymLimits.MAX_NOTES_LENGTH)?.ifEmpty { null })
}

class DiscardGymSessionUseCase @Inject constructor(
    private val repository: GymRepository
) {
    suspend operator fun invoke(sessionId: String) = repository.discardSession(sessionId)
}

class SearchExercisesUseCase @Inject constructor(
    private val repository: GymRepository
) {
    suspend operator fun invoke(
        query: String,
        muscleGroup: MuscleGroup? = null,
        limit: Int = GymLimits.DEFAULT_SEARCH_LIMIT
    ): List<Exercise> = repository.searchExercises(query, muscleGroup, limit)
}

class GetExercisesByIdsUseCase @Inject constructor(
    private val repository: GymRepository
) {
    suspend operator fun invoke(ids: Collection<String>): Map<String, Exercise> =
        if (ids.isEmpty()) emptyMap() else repository.getExercises(ids).associateBy { it.id }
}

class ObserveGymHistoryUseCase @Inject constructor(
    private val repository: GymRepository
) {
    operator fun invoke(limit: Int = DEFAULT_LIMIT): Flow<List<GymSessionSummary>> =
        repository.observeHistory(limit)

    companion object {
        const val DEFAULT_LIMIT = 30
    }
}

class ObserveGymVolumeTrendUseCase @Inject constructor(
    private val repository: GymRepository
) {
    operator fun invoke(fromEpochDay: Long): Flow<List<DailyGymVolume>> =
        repository.observeDailyVolume(fromEpochDay)
}

class ObserveExerciseHistoryUseCase @Inject constructor(
    private val repository: GymRepository
) {
    operator fun invoke(exerciseId: String, limit: Int = DEFAULT_LIMIT): Flow<List<GymSet>> =
        repository.observeExerciseHistory(exerciseId, limit)

    companion object {
        const val DEFAULT_LIMIT = 200
    }
}
