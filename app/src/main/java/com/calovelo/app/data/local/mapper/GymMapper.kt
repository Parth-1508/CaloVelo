// app/src/main/java/com/calovelo/app/data/local/mapper/GymMapper.kt
package com.calovelo.app.data.local.mapper

import com.calovelo.app.data.local.dao.DailyVolumeRow
import com.calovelo.app.data.local.dao.GymSessionAggregateRow
import com.calovelo.app.data.local.entity.ExerciseCatalogEntity
import com.calovelo.app.data.local.entity.ExerciseSetEntity
import com.calovelo.app.data.local.entity.GymWorkoutEntity
import com.calovelo.app.domain.model.DailyGymVolume
import com.calovelo.app.domain.model.EquipmentType
import com.calovelo.app.domain.model.Exercise
import com.calovelo.app.domain.model.GymSession
import com.calovelo.app.domain.model.GymSessionSummary
import com.calovelo.app.domain.model.GymSet
import com.calovelo.app.domain.model.MuscleGroup
import com.calovelo.app.domain.model.SetType

fun ExerciseCatalogEntity.toDomain(): Exercise = Exercise(
    id = exerciseId,
    name = name,
    muscleGroup = MuscleGroup.fromKey(muscleGroup),
    equipmentType = EquipmentType.fromKey(equipmentType),
    instructions = instructions
)

fun GymWorkoutEntity.toDomain(): GymSession = GymSession(
    sessionId = sessionId,
    epochDay = epochDay,
    startTimeMillis = startTime,
    endTimeMillis = endTime,
    notes = notes
)

fun ExerciseSetEntity.toDomain(): GymSet = GymSet(
    setId = setId,
    sessionId = workoutId,
    exerciseId = exerciseId,
    setIndex = setIndex,
    weightKg = weightKg,
    reps = reps,
    rpe = rpe,
    type = SetType.fromName(setType)
)

fun GymSessionAggregateRow.toDomain(): GymSessionSummary = GymSessionSummary(
    session = GymSession(
        sessionId = sessionId,
        epochDay = epochDay,
        startTimeMillis = startTime,
        endTimeMillis = endTime,
        notes = notes
    ),
    setCount = setCount,
    totalVolumeKg = volumeKg
)

fun DailyVolumeRow.toDomain(): DailyGymVolume = DailyGymVolume(epochDay = epochDay, volumeKg = volumeKg)

/**
 * Builds the LIKE patterns the exercise search needs from free text. User input is escaped so `%`
 * and `_` match literally (the DAO query uses `ESCAPE '\'`).
 */
data class ExerciseSearchQuery(
    val token1: String,
    val token2: String,
    val token3: String,
    val prefixPattern: String
) {
    companion object {
        private const val MAX_TOKENS = 3
        private const val MATCH_ALL = "%"

        fun from(raw: String): ExerciseSearchQuery {
            val tokens = raw.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.take(MAX_TOKENS)
            fun pattern(index: Int) = tokens.getOrNull(index)?.let { "%${escape(it)}%" } ?: MATCH_ALL
            return ExerciseSearchQuery(
                token1 = pattern(0),
                token2 = pattern(1),
                token3 = pattern(2),
                prefixPattern = tokens.firstOrNull()?.let { "${escape(it)}%" } ?: MATCH_ALL
            )
        }

        private fun escape(token: String): String =
            token.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
    }
}
