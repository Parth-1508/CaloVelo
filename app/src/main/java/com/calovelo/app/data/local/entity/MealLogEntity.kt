// app/src/main/java/com/calovelo/app/data/local/entity/MealLogEntity.kt
package com.calovelo.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * A meal row, either eaten ([STATUS_LOGGED]) or planned for later ([STATUS_PLANNED]). Nutrient
 * columns are a snapshot taken when the row was written, so editing the catalog never changes
 * history. [foodLabelKey] is deliberately not a foreign key: logged meals must survive catalog changes.
 */
@Entity(
    tableName = "meal_log",
    indices = [Index(value = ["date_epoch_day", "status"])]
)
data class MealLogEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "food_label_key") val foodLabelKey: String,
    @ColumnInfo(name = "food_name") val foodName: String,
    /** MealType name: BREAKFAST, LUNCH, DINNER or SNACK. */
    @ColumnInfo(name = "meal_type") val mealType: String,
    /** [STATUS_LOGGED] or [STATUS_PLANNED]. */
    @ColumnInfo(name = "status") val status: String,
    /** MealSource name: MANUAL, SCANNER or AUTO_PLAN. */
    @ColumnInfo(name = "source") val source: String,
    /** Local calendar day (LocalDate.toEpochDay) the meal belongs to. */
    @ColumnInfo(name = "date_epoch_day") val dateEpochDay: Long,
    /** Epoch millis when the row was created (planned) or eaten (logged). */
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "grams") val grams: Double,
    @ColumnInfo(name = "kcal") val kcal: Double,
    @ColumnInfo(name = "protein_g") val proteinG: Double,
    @ColumnInfo(name = "carbs_g") val carbsG: Double,
    @ColumnInfo(name = "fat_g") val fatG: Double,
    @ColumnInfo(name = "fiber_g") val fiberG: Double
) {
    companion object {
        // Must equal MealStatus.name / MealSource.name; verified by NutritionMappersTest.
        const val STATUS_LOGGED = "LOGGED"
        const val STATUS_PLANNED = "PLANNED"
        const val SOURCE_AUTO_PLAN = "AUTO_PLAN"
    }
}
