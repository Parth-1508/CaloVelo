// app/src/main/java/com/calovelo/app/presentation/nutrition/NutritionViewModel.kt
package com.calovelo.app.presentation.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calovelo.app.domain.model.DailyNutritionSummary
import com.calovelo.app.domain.model.MealLog
import com.calovelo.app.domain.model.MealType
import com.calovelo.app.domain.nutrition.MacroCalculator
import com.calovelo.app.domain.usecase.ClearTodayPlanUseCase
import com.calovelo.app.domain.usecase.DeleteMealUseCase
import com.calovelo.app.domain.usecase.GenerateDailyPlanUseCase
import com.calovelo.app.domain.usecase.MarkPlannedMealEatenUseCase
import com.calovelo.app.domain.usecase.ObserveDailyNutritionUseCase
import com.calovelo.app.domain.usecase.PlanGenerationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class NutritionViewModel @Inject constructor(
    observeDailyNutrition: ObserveDailyNutritionUseCase,
    private val generateDailyPlan: GenerateDailyPlanUseCase,
    private val markPlannedMealEaten: MarkPlannedMealEatenUseCase,
    private val deleteMeal: DeleteMealUseCase,
    private val clearTodayPlan: ClearTodayPlanUseCase
) : ViewModel() {

    private val isGenerating = MutableStateFlow(false)
    private val userMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<NutritionUiState> = combine(
        observeDailyNutrition(),
        isGenerating,
        userMessage
    ) { summary, generating, message ->
        summary.toUiState(isGenerating = generating, userMessage = message)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = NutritionUiState()
    )

    fun onGeneratePlan() {
        if (!isGenerating.compareAndSet(expect = false, update = true)) return
        viewModelScope.launch {
            try {
                userMessage.value = when (val result = generateDailyPlan()) {
                    is PlanGenerationResult.Generated -> {
                        val meals = result.suggestions.map { it.mealType }.toSet().size
                        "Plan ready: ${result.suggestions.size} foods across $meals meals."
                    }
                    PlanGenerationResult.GoalAlreadyReached ->
                        "You've reached today's calorie goal, so there's nothing left to plan."
                    PlanGenerationResult.NoEligibleFoods ->
                        "No foods in the catalog fit the remaining meals."
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                userMessage.value = "Couldn't generate a plan. Please try again."
            } finally {
                isGenerating.value = false
            }
        }
    }

    fun onMarkEaten(mealId: String) = runCatchingMessage("Couldn't mark that meal as eaten.") {
        if (!markPlannedMealEaten(mealId)) userMessage.value = "That meal was already updated."
    }

    fun onDeleteMeal(mealId: String) = runCatchingMessage("Couldn't remove that meal.") {
        deleteMeal(mealId)
    }

    fun onClearPlan() = runCatchingMessage("Couldn't clear the plan.") { clearTodayPlan() }

    fun onMessageShown() {
        userMessage.value = null
    }

    private fun runCatchingMessage(failure: String, block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                userMessage.value = failure
            }
        }
    }
}

internal fun DailyNutritionSummary.toUiState(isGenerating: Boolean, userMessage: String?): NutritionUiState {
    fun ring(label: String, consumed: Double, goal: Int, unit: String): MacroRingUi {
        val fraction = MacroCalculator.progress(consumed, goal.toDouble())
        return MacroRingUi(
            label = label,
            consumed = consumed.roundToInt(),
            goal = goal,
            unit = unit,
            progress = fraction.coerceIn(0f, 1f),
            isOverGoal = goal > 0 && fraction > 1f
        )
    }

    fun section(type: MealType, meals: List<MealLog>): MealSectionUi {
        val rows = meals.filter { it.mealType == type }
        return MealSectionUi(
            mealType = type,
            kcal = rows.sumOf { it.macros.kcal }.roundToInt(),
            rows = rows.map { it.toRow() }
        )
    }

    return NutritionUiState(
        isLoading = false,
        calories = ring("Calories", consumed.kcal, goals.kcal, "kcal"),
        protein = ring("Protein", consumed.proteinG, goals.proteinG, "g"),
        carbs = ring("Carbs", consumed.carbsG, goals.carbsG, "g"),
        fat = ring("Fat", consumed.fatG, goals.fatG, "g"),
        plannedKcal = planned.kcal.roundToInt(),
        remainingKcal = remaining.kcal.roundToInt(),
        remainingProteinG = remaining.proteinG.roundToInt(),
        plannedSections = MealType.entries
            .map { section(it, plannedMeals) }
            .filter { it.rows.isNotEmpty() },
        loggedSections = MealType.entries.map { section(it, loggedMeals) },
        isGenerating = isGenerating,
        userMessage = userMessage
    )
}

private fun MealLog.toRow() = MealRowUi(
    id = id,
    name = foodName,
    grams = grams.roundToInt(),
    kcal = macros.kcal.roundToInt(),
    proteinG = macros.proteinG.roundToInt()
)
