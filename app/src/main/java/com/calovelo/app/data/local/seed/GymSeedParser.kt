// app/src/main/java/com/calovelo/app/data/local/seed/GymSeedParser.kt
package com.calovelo.app.data.local.seed

import com.calovelo.app.data.local.entity.ExerciseCatalogEntity
import com.calovelo.app.domain.model.EquipmentType
import com.calovelo.app.domain.model.MuscleGroup
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/** Shape of one element of assets/gym_seed.json. */
@Serializable
internal data class ExerciseSeedDto(
    val exerciseId: String,
    val name: String,
    val muscleGroup: String,
    val equipmentType: String,
    val instructions: String = ""
)

/**
 * Parses and validates the bundled exercise catalog. Pure JVM (no Android types), so it runs in
 * unit tests, which is what guards the shipped JSON against typos.
 *
 * The shipped file contains a handful of repeated exerciseIds (mostly identical rows). Repeats are
 * collapsed, keeping the first occurrence, rather than rejected: the id is the primary key, and a
 * strict check would stop the whole catalog from loading over a cosmetic duplicate.
 */
object GymSeedParser {

    private val json = Json { ignoreUnknownKeys = true }
    private val EXERCISE_ID = Regex("[a-z0-9_]+")

    /** @throws IllegalArgumentException (or a SerializationException) when the JSON is malformed or invalid. */
    fun parse(text: String): List<ExerciseCatalogEntity> {
        val dtos = json.decodeFromString(ListSerializer(ExerciseSeedDto.serializer()), text)
        require(dtos.isNotEmpty()) { "Gym seed is empty." }

        val byId = LinkedHashMap<String, ExerciseCatalogEntity>(dtos.size)
        for (dto in dtos) {
            val entity = dto.toEntity()
            if (!byId.containsKey(entity.exerciseId)) byId[entity.exerciseId] = entity
        }
        return byId.values.toList()
    }

    private fun ExerciseSeedDto.toEntity(): ExerciseCatalogEntity {
        val id = "'$exerciseId'"
        require(EXERCISE_ID.matches(exerciseId)) { "exerciseId $id must only contain [a-z0-9_]." }
        require(name.isNotBlank()) { "$id has a blank name." }
        val muscle = requireNotNull(MuscleGroup.fromKeyOrNull(muscleGroup)) { "$id has unknown muscleGroup '$muscleGroup'." }
        val equipment = requireNotNull(EquipmentType.fromKeyOrNull(equipmentType)) { "$id has unknown equipmentType '$equipmentType'." }

        return ExerciseCatalogEntity(
            exerciseId = exerciseId,
            name = name.trim(),
            muscleGroup = muscle.key,
            equipmentType = equipment.key,
            instructions = instructions.trim()
        )
    }
}
