// app/src/main/java/com/calovelo/app/data/local/mapper/FoodMapper.kt
package com.calovelo.app.data.local.mapper

import com.calovelo.app.data.local.entity.FoodCatalogEntity
import com.calovelo.app.domain.model.FoodCategory
import com.calovelo.app.domain.model.FoodItem
import com.calovelo.app.domain.model.MealType

/** Meal types are stored as one comma-separated column; an empty string means "never auto-suggest". */
object MealTypeCodec {
    fun encode(types: Set<MealType>): String =
        types.sortedBy { it.ordinal }.joinToString(",") { it.name }

    fun decode(raw: String): Set<MealType> =
        raw.split(',').mapNotNull { MealType.fromName(it.trim()) }.toSet()
}

fun FoodCatalogEntity.toDomain(): FoodItem = FoodItem(
    labelKey = labelKey,
    displayName = displayName,
    category = FoodCategory.fromKey(category),
    defaultServingGrams = defaultServingGrams,
    kcalPer100g = kcalPer100g,
    proteinPer100g = proteinPer100g,
    carbsPer100g = carbsPer100g,
    fatPer100g = fatPer100g,
    fiberPer100g = fiberPer100g,
    suitableMealTypes = MealTypeCodec.decode(suitableMealTypes),
    isVeg = isVeg,
    isVegan = isVegan,
    isEgg = isEgg,
    isNonVeg = isNonVeg,
    containsLactose = containsLactose
)

/**
 * Builds the three LIKE patterns the catalog search needs from free text. User input is escaped
 * so `%` and `_` are matched literally.
 */
data class FoodSearchQuery(
    val token1: String,
    val token2: String,
    val token3: String,
    val prefixPattern: String
) {
    companion object {
        private const val MAX_TOKENS = 3
        private const val MATCH_ALL = "%"

        fun from(raw: String): FoodSearchQuery {
            val tokens = raw.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.take(MAX_TOKENS)
            fun pattern(index: Int) = tokens.getOrNull(index)?.let { "%${escape(it)}%" } ?: MATCH_ALL
            return FoodSearchQuery(
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
