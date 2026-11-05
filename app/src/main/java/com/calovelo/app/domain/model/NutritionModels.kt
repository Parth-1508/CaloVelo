// app/src/main/java/com/calovelo/app/domain/model/NutritionModels.kt
package com.calovelo.app.domain.model

import java.io.IOException
import java.io.InputStream
import java.time.LocalTime
import kotlin.math.max
import kotlin.math.roundToInt

enum class MealType(val displayName: String) {
    BREAKFAST("Breakfast"),
    LUNCH("Lunch"),
    DINNER("Dinner"),
    SNACK("Snack");

    companion object {
        fun fromName(name: String?): MealType? = entries.firstOrNull { it.name == name }

        /** The meal a user is most likely logging at [time]. */
        fun suggestedFor(time: LocalTime): MealType = when (time.hour) {
            in 4..10 -> BREAKFAST
            in 11..15 -> LUNCH
            in 16..17 -> SNACK
            in 18..22 -> DINNER
            else -> SNACK
        }
    }
}

/** A meal row is either already eaten (LOGGED) or a suggestion still to be eaten (PLANNED). */
enum class MealStatus { LOGGED, PLANNED;

    companion object {
        fun fromName(name: String?): MealStatus = entries.firstOrNull { it.name == name } ?: LOGGED
    }
}

enum class MealSource { MANUAL, SCANNER, AUTO_PLAN;

    companion object {
        fun fromName(name: String?): MealSource = entries.firstOrNull { it.name == name } ?: MANUAL
    }
}

enum class FoodCategory(val key: String, val displayName: String) {
    GRAIN("grain", "Grains & bread"),
    BREAKFAST("breakfast", "Breakfast"),
    LEGUME("legume", "Dals & legumes"),
    PROTEIN("protein", "Protein"),
    DAIRY("dairy", "Dairy"),
    VEGETABLE("vegetable", "Vegetables"),
    FRUIT("fruit", "Fruit"),
    SNACK("snack", "Snacks & nuts"),
    MIXED_MEAL("mixed_meal", "Mixed meals"),
    OTHER("other", "Other");

    companion object {
        fun fromKey(key: String?): FoodCategory {
            if (key == null) return OTHER
            val trimmed = key.trim()
            entries.firstOrNull { it.key.equals(trimmed, ignoreCase = true) }?.let { return it }
            entries.firstOrNull { it.displayName.equals(trimmed, ignoreCase = true) }?.let { return it }
            return when (trimmed.lowercase()) {
                "breads", "grains", "rice dishes", "staples" -> GRAIN
                "curries & dal", "dals & legumes", "curries", "legumes" -> LEGUME
                "poultry & meat", "meat", "poultry", "proteins" -> PROTEIN
                "eggs & dairy", "dairy" -> DAIRY
                "south indian", "mixed meals" -> MIXED_MEAL
                "nuts & seeds", "snacks" -> SNACK
                "fruits" -> FRUIT
                "vegetables" -> VEGETABLE
                "breakfast" -> BREAKFAST
                else -> OTHER
            }
        }
    }
}

/** Energy and macronutrients. Grams for every nutrient except [kcal]. */
data class Macros(
    val kcal: Double = 0.0,
    val proteinG: Double = 0.0,
    val carbsG: Double = 0.0,
    val fatG: Double = 0.0,
    val fiberG: Double = 0.0
) {
    operator fun plus(other: Macros) = Macros(
        kcal + other.kcal,
        proteinG + other.proteinG,
        carbsG + other.carbsG,
        fatG + other.fatG,
        fiberG + other.fiberG
    )

    operator fun minus(other: Macros) = Macros(
        kcal - other.kcal,
        proteinG - other.proteinG,
        carbsG - other.carbsG,
        fatG - other.fatG,
        fiberG - other.fiberG
    )

    operator fun times(factor: Double) = Macros(
        kcal * factor,
        proteinG * factor,
        carbsG * factor,
        fatG * factor,
        fiberG * factor
    )

    fun coerceAtLeastZero() = Macros(
        max(kcal, 0.0),
        max(proteinG, 0.0),
        max(carbsG, 0.0),
        max(fatG, 0.0),
        max(fiberG, 0.0)
    )

    companion object {
        val ZERO = Macros()
    }
}

/** One catalog entry. Nutrient values are per 100 g of the food as typically eaten (cooked/ready). */
data class FoodItem(
    val labelKey: String,
    val displayName: String,
    val category: FoodCategory,
    val defaultServingGrams: Int,
    val kcalPer100g: Double,
    val proteinPer100g: Double,
    val carbsPer100g: Double,
    val fatPer100g: Double,
    val fiberPer100g: Double,
    /** Meals the auto-planner may suggest this food for. Empty = never auto-suggested. */
    val suitableMealTypes: Set<MealType> = MealType.entries.toSet()
) {
    val per100g: Macros
        get() = Macros(kcalPer100g, proteinPer100g, carbsPer100g, fatPer100g, fiberPer100g)
}

/** Axis-aligned box. Coordinates are normalized (0..1) unless the producer documents otherwise. */
data class BoundingBox(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    val width: Float get() = max(right - left, 0f)
    val height: Float get() = max(bottom - top, 0f)
    val area: Float get() = width * height

    fun iou(other: BoundingBox): Float {
        val interW = max(minOf(right, other.right) - max(left, other.left), 0f)
        val interH = max(minOf(bottom, other.bottom) - max(top, other.top), 0f)
        val inter = interW * interH
        val union = area + other.area - inter
        return if (union <= 0f) 0f else inter / union
    }
}

/**
 * A food recognised in a photo.
 * @property box normalized to the upright (EXIF-corrected) source image.
 * @property portionMultiplier estimate of serving size relative to [FoodItem.defaultServingGrams].
 */
data class DetectedFoodCandidate(
    val food: FoodItem,
    val confidence: Float,
    val box: BoundingBox,
    val portionMultiplier: Float
) {
    /** Suggested portion in grams, rounded to 5 g. */
    val suggestedGrams: Int
        get() = ((food.defaultServingGrams * portionMultiplier / 5f).roundToInt() * 5).coerceAtLeast(5)
}

data class MealLog(
    val id: String,
    val foodLabelKey: String,
    val foodName: String,
    val mealType: MealType,
    val status: MealStatus,
    val source: MealSource,
    val epochDay: Long,
    val timestampMillis: Long,
    val grams: Double,
    /** Snapshot taken when the meal was saved, so later catalog edits never rewrite history. */
    val macros: Macros
)

data class MacroGoals(
    val kcal: Int,
    val proteinG: Int,
    val carbsG: Int,
    val fatG: Int
) {
    fun toMacros() = Macros(kcal.toDouble(), proteinG.toDouble(), carbsG.toDouble(), fatG.toDouble())

    companion object {
        const val PROTEIN_KCAL_SHARE = 0.25
        const val CARBS_KCAL_SHARE = 0.45
        const val FAT_KCAL_SHARE = 0.30
        private const val KCAL_PER_G_PROTEIN_CARBS = 4.0
        private const val KCAL_PER_G_FAT = 9.0

        /** Derives gram targets from a calorie goal using a 25 / 45 / 30 (P / C / F) energy split. */
        fun fromCalories(kcal: Int): MacroGoals {
            val safe = kcal.coerceAtLeast(0)
            return MacroGoals(
                kcal = safe,
                proteinG = (safe * PROTEIN_KCAL_SHARE / KCAL_PER_G_PROTEIN_CARBS).roundToInt(),
                carbsG = (safe * CARBS_KCAL_SHARE / KCAL_PER_G_PROTEIN_CARBS).roundToInt(),
                fatG = (safe * FAT_KCAL_SHARE / KCAL_PER_G_FAT).roundToInt()
            )
        }
    }
}

/**
 * @property consumed totals of LOGGED meals only.
 * @property planned totals of PLANNED meals still to be eaten.
 * @property remaining goals minus [consumed], never negative. Planned meals do not reduce it,
 * which is what lets the planner be re-run without double counting its own previous output.
 */
data class DailyNutritionSummary(
    val epochDay: Long,
    val goals: MacroGoals,
    val meals: List<MealLog>,
    val consumed: Macros,
    val planned: Macros,
    val remaining: Macros
) {
    val loggedMeals: List<MealLog> get() = meals.filter { it.status == MealStatus.LOGGED }
    val plannedMeals: List<MealLog> get() = meals.filter { it.status == MealStatus.PLANNED }
}

/** One food the auto-planner proposes for [mealType]. */
data class MealPlanSuggestion(
    val mealType: MealType,
    val food: FoodItem,
    val grams: Int,
    val macros: Macros
)

/** A food + portion the user wants to log. */
data class MealEntry(
    val food: FoodItem,
    val grams: Int,
    val source: MealSource = MealSource.MANUAL
)

/** Opens a fresh stream on every call so the image can be read more than once (bounds, EXIF, pixels). */
fun interface ImageSource {
    @Throws(IOException::class)
    fun open(): InputStream
}

sealed interface FoodScanResult {
    /** At least one food was recognised and matched to the catalog. */
    data class Detected(
        val candidates: List<DetectedFoodCandidate>,
        val imageWidth: Int,
        val imageHeight: Int
    ) : FoodScanResult {
        val aspectRatio: Float
            get() = if (imageHeight > 0) imageWidth.toFloat() / imageHeight else 1f
    }

    /** The model ran but found nothing it could match to a catalog food. */
    data class NothingDetected(val unmappedLabels: List<String> = emptyList()) : FoodScanResult

    /** food_detector.tflite / food_labels.txt are missing or unusable; the user picks from the catalog. */
    data class ModelUnavailable(val reason: String) : FoodScanResult

    data class Failure(val message: String) : FoodScanResult
}
