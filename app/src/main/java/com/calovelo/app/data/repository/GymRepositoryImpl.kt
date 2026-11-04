// app/src/main/java/com/calovelo/app/data/repository/GymRepositoryImpl.kt
package com.calovelo.app.data.repository

import androidx.room.withTransaction
import com.calovelo.app.data.local.CaloveloDatabase
import com.calovelo.app.data.local.dao.ExerciseCatalogDao
import com.calovelo.app.data.local.dao.GymWorkoutDao
import com.calovelo.app.data.local.entity.ExerciseSetEntity
import com.calovelo.app.data.local.entity.GymWorkoutEntity
import com.calovelo.app.data.local.mapper.ExerciseSearchQuery
import com.calovelo.app.data.local.mapper.toDomain
import com.calovelo.app.data.local.seed.GymCatalogSeeder
import com.calovelo.app.domain.gym.StrengthAnalyticsEngine
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
import com.calovelo.app.domain.repository.GymRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GymRepositoryImpl @Inject constructor(
    private val database: CaloveloDatabase,
    private val catalogDao: ExerciseCatalogDao,
    private val workoutDao: GymWorkoutDao,
    private val seeder: GymCatalogSeeder,
    private val analytics: StrengthAnalyticsEngine,
    private val clock: Clock
) : GymRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeActiveSession(): Flow<ActiveGymSession?> =
        workoutDao.observeActive().flatMapLatest { workout ->
            if (workout == null) {
                flowOf(null)
            } else {
                workoutDao.observeSets(workout.sessionId).map { sets ->
                    ActiveGymSession(session = workout.toDomain(), sets = sets.map { it.toDomain() })
                }
            }
        }

    override fun observeHistory(limit: Int): Flow<List<GymSessionSummary>> =
        workoutDao.observeFinishedSessions(limit.coerceIn(1, MAX_HISTORY_LIMIT))
            .map { rows -> rows.map { it.toDomain() } }

    override fun observeDailyVolume(fromEpochDay: Long): Flow<List<DailyGymVolume>> =
        workoutDao.observeDailyVolume(fromEpochDay).map { rows -> rows.map { it.toDomain() } }

    override fun observeExerciseHistory(exerciseId: String, limit: Int): Flow<List<GymSet>> =
        workoutDao.observeExerciseHistory(exerciseId, limit.coerceIn(1, MAX_HISTORY_LIMIT))
            .map { rows -> rows.map { it.toDomain() } }

    override suspend fun startSession(): GymSession = database.withTransaction {
        workoutDao.getActive()?.let { return@withTransaction it.toDomain() }
        val workout = GymWorkoutEntity(
            epochDay = LocalDate.now(clock).toEpochDay(),
            startTime = clock.millis(),
            endTime = null,
            notes = null
        )
        workoutDao.insertWorkout(workout)
        workout.toDomain()
    }

    override suspend fun logSet(
        sessionId: String,
        exerciseId: String,
        weightKg: Double,
        reps: Int,
        rpe: Double?,
        type: SetType
    ): LogSetResult {
        seeder.ensureSeeded()
        return database.withTransaction {
            val workout = workoutDao.getById(sessionId)
            if (workout == null || workout.endTime != null) return@withTransaction LogSetResult.NoActiveSession
            if (catalogDao.getById(exerciseId) == null) return@withTransaction LogSetResult.UnknownExercise

            val entity = ExerciseSetEntity(
                workoutId = sessionId,
                exerciseId = exerciseId,
                setIndex = (workoutDao.getMaxSetIndex(sessionId, exerciseId) ?: 0) + 1,
                weightKg = weightKg,
                reps = reps,
                rpe = rpe,
                setType = type.name
            )
            workoutDao.insertSet(entity)
            LogSetResult.Logged(entity.toDomain())
        }
    }

    override suspend fun updateSet(setId: String, weightKg: Double, reps: Int, rpe: Double?, type: SetType): Boolean =
        database.withTransaction {
            val existing = workoutDao.getSet(setId) ?: return@withTransaction false
            if (!isActive(existing.workoutId)) return@withTransaction false
            workoutDao.updateSetValues(setId, weightKg, reps, rpe, type.name) > 0
        }

    override suspend fun deleteSet(setId: String): Boolean = database.withTransaction {
        val existing = workoutDao.getSet(setId) ?: return@withTransaction false
        if (!isActive(existing.workoutId)) return@withTransaction false
        if (workoutDao.deleteSet(setId) == 0) return@withTransaction false
        workoutDao.closeSetIndexGap(existing.workoutId, existing.exerciseId, existing.setIndex)
        true
    }

    override suspend fun finishSession(sessionId: String, notes: String?): FinishGymResult =
        database.withTransaction {
            val workout = workoutDao.getById(sessionId)
            if (workout == null || workout.endTime != null) return@withTransaction FinishGymResult.NoActiveSession

            val sets = workoutDao.getSets(sessionId)
            if (sets.isEmpty()) {
                workoutDao.deleteWorkout(sessionId)
                return@withTransaction FinishGymResult.DiscardedEmpty
            }

            val endTime = clock.millis().coerceAtLeast(workout.startTime)
            if (workoutDao.finishWorkout(sessionId, endTime, notes) == 0) {
                return@withTransaction FinishGymResult.NoActiveSession
            }
            FinishGymResult.Finished(
                GymSessionSummary(
                    session = workout.copy(endTime = endTime, notes = notes).toDomain(),
                    setCount = sets.size,
                    totalVolumeKg = analytics.totalSessionVolume(sets.map { it.toDomain() })
                )
            )
        }

    override suspend fun discardSession(sessionId: String) {
        workoutDao.deleteWorkout(sessionId)
    }

    override suspend fun searchExercises(query: String, muscleGroup: MuscleGroup?, limit: Int): List<Exercise> {
        seeder.ensureSeeded()
        val q = ExerciseSearchQuery.from(query)
        return catalogDao
            .search(
                token1 = q.token1,
                token2 = q.token2,
                token3 = q.token3,
                prefixPattern = q.prefixPattern,
                muscleGroup = muscleGroup?.key,
                limit = limit.coerceIn(1, GymLimits.MAX_SEARCH_LIMIT)
            )
            .map { it.toDomain() }
    }

    override suspend fun getExercises(ids: Collection<String>): List<Exercise> {
        if (ids.isEmpty()) return emptyList()
        seeder.ensureSeeded()
        // Chunked to stay far below SQLite's bound-variable limit.
        return ids.distinct().chunked(ID_CHUNK).flatMap { chunk -> catalogDao.getByIds(chunk) }.map { it.toDomain() }
    }

    /** True only while the workout exists and has not been finished. */
    private suspend fun isActive(workoutId: String): Boolean =
        workoutDao.getById(workoutId)?.let { it.endTime == null } ?: false

    private companion object {
        const val MAX_HISTORY_LIMIT = 500
        const val ID_CHUNK = 500
    }
}
