// app/src/main/java/com/calovelo/app/presentation/nutrition/NutritionUiState.kt
package com.calovelo.app.presentation.insights

import com.calovelo.app.domain.model.MealType

data class MacroRingUi(
    val label: String,
    val consumed: Int,
    val goal: Int,
    val unit: String,
    /** 0f..1f, clamped for the indicator. */
    val progress: Float,
    val isOverGoal: Boolean
)

data class MealRowUi(
    val id: String,
    val name: String,
    val grams: Int,
    val kcal: Int,
    val proteinG: Int
)

/** One meal slot (breakfast, lunch, ...) with its rows and subtotal. */
data class MealSectionUi(
    val mealType: MealType,
    val kcal: Int,
    val rows: List<MealRowUi>
)

data class NutritionUiState(
    val isLoading: Boolean = true,
    val calories: MacroRingUi = MacroRingUi("Calories", 0, 0, "kcal", 0f, false),
    val protein: MacroRingUi = MacroRingUi("Protein", 0, 0, "g", 0f, false),
    val carbs: MacroRingUi = MacroRingUi("Carbs", 0, 0, "g", 0f, false),
    val fat: MacroRingUi = MacroRingUi("Fat", 0, 0, "g", 0f, false),
    val plannedKcal: Int = 0,
    val remainingKcal: Int = 0,
    val remainingProteinG: Int = 0,
    /** Planned (not yet eaten) meals grouped by slot; slots without planned meals are omitted. */
    val plannedSections: List<MealSectionUi> = emptyList(),
    /** Always four sections in meal order, so empty slots can show an "add" affordance. */
    val loggedSections: List<MealSectionUi> = emptyList(),
    val isGenerating: Boolean = false,
    val userMessage: String? = null
)
