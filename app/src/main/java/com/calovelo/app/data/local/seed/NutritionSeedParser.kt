// app/src/main/java/com/calovelo/app/data/local/seed/NutritionSeedParser.kt
package com.calovelo.app.data.local.seed

import com.calovelo.app.data.local.entity.FoodCatalogEntity
import com.calovelo.app.data.local.mapper.MealTypeCodec
import com.calovelo.app.domain.model.FoodCategory
import com.calovelo.app.domain.model.MealType
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/** Shape of one element of assets/nutrition_seed.json. */
@Serializable
internal data class FoodSeedDto(
    val labelKey: String,
    val displayName: String,
    val category: String,
    val defaultServingGrams: Int,
    val kcalPer100g: Double,
    val proteinPer100g: Double,
    val carbsPer100g: Double,
    val fatPer100g: Double,
    val fiberPer100g: Double = 0.0,
    /** Meals the planner may use the food for. Missing = all meals; empty list = never auto-suggest. */
    val mealTypes: List<String>? = null
)

/**
 * Parses and validates the bundled catalog. Pure JVM (no Android types) so it runs in unit tests,
 * which is what guards the shipped JSON against typos.
 */
object NutritionSeedParser {

    private val json = Json { ignoreUnknownKeys = true }
    private val LABEL_KEY = Regex("[a-z0-9]+(_[a-z0-9]+)*")

    /** @throws IllegalArgumentException (or a SerializationException) when the JSON is malformed or invalid. */
    fun parse(text: String): List<FoodCatalogEntity> {
        val dtos = json.decodeFromString(ListSerializer(FoodSeedDto.serializer()), text)
        require(dtos.isNotEmpty()) { "Nutrition seed is empty." }

        val seen = HashSet<String>()
        return dtos.map { dto ->
            require(seen.add(dto.labelKey)) { "Duplicate labelKey '${dto.labelKey}'." }
            dto.toEntity()
        }
    }

    private fun FoodSeedDto.toEntity(): FoodCatalogEntity {
        val id = "'$labelKey'"
        require(LABEL_KEY.matches(labelKey)) { "labelKey $id must be snake_case [a-z0-9_]." }
        require(displayName.isNotBlank()) { "$id has a blank displayName." }
        val foodCategory = FoodCategory.entries.firstOrNull { it.key == category }
        requireNotNull(foodCategory) { "$id has unknown category '$category'." }
        require(defaultServingGrams in 1..2_000) { "$id defaultServingGrams $defaultServingGrams is out of range." }
        listOf(kcalPer100g, proteinPer100g, carbsPer100g, fatPer100g, fiberPer100g).forEach {
            require(it.isFinite() && it >= 0.0) { "$id has a negative or non-finite nutrient value." }
        }
        require(kcalPer100g > 0.0) { "$id has zero calories." }
        require(kcalPer100g <= 900.0) { "$id has more than 900 kcal/100 g, which is physically impossible." }
        require(proteinPer100g + carbsPer100g + fatPer100g <= 100.0) { "$id macros exceed 100 g per 100 g." }

        val types: Set<MealType> = mealTypes
            ?.map { name -> requireNotNull(MealType.fromName(name)) { "$id has unknown meal type '$name'." } }
            ?.toSet()
            ?: MealType.entries.toSet()

        return FoodCatalogEntity(
            labelKey = labelKey,
            displayName = displayName.trim(),
            category = foodCategory.key,
            defaultServingGrams = defaultServingGrams,
            kcalPer100g = kcalPer100g,
            proteinPer100g = proteinPer100g,
            carbsPer100g = carbsPer100g,
            fatPer100g = fatPer100g,
            fiberPer100g = fiberPer100g,
            suitableMealTypes = MealTypeCodec.encode(types),
            source = FoodCatalogEntity.SOURCE_SEED
        )
    }
}
