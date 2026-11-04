// app/src/main/java/com/calovelo/app/data/local/entity/FoodCatalogEntity.kt
package com.calovelo.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One food in the local nutrition catalog. [labelKey] is the stable identifier shared with the
 * detector's food_labels.txt (normalized to snake_case). Nutrient values are per 100 g.
 */
@Entity(
    tableName = "food_catalog",
    indices = [Index(value = ["display_name"]), Index(value = ["category"])]
)
data class FoodCatalogEntity(
    @PrimaryKey
    @ColumnInfo(name = "label_key") val labelKey: String,
    @ColumnInfo(name = "display_name") val displayName: String,
    @ColumnInfo(name = "category") val category: String,
    @ColumnInfo(name = "default_serving_grams") val defaultServingGrams: Int,
    @ColumnInfo(name = "kcal_per_100g") val kcalPer100g: Double,
    @ColumnInfo(name = "protein_per_100g") val proteinPer100g: Double,
    @ColumnInfo(name = "carbs_per_100g") val carbsPer100g: Double,
    @ColumnInfo(name = "fat_per_100g") val fatPer100g: Double,
    @ColumnInfo(name = "fiber_per_100g") val fiberPer100g: Double,
    /** Comma-separated MealType names the auto-planner may use this food for; empty = never. */
    @ColumnInfo(name = "suitable_meal_types") val suitableMealTypes: String,
    /** Where the row came from: [SOURCE_SEED] today, room for user-created foods later. */
    @ColumnInfo(name = "source") val source: String
) {
    companion object {
        const val SOURCE_SEED = "SEED"
    }
}
