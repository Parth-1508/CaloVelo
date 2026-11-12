// app/src/main/java/com/calovelo/app/domain/usecase/NutritionUseCases.kt
package com.calovelo.app.domain.usecase

import com.calovelo.app.domain.model.DailyNutritionSummary
import com.calovelo.app.domain.model.FoodItem
import com.calovelo.app.domain.model.FoodScanResult
import com.calovelo.app.domain.model.ImageSource
import com.calovelo.app.domain.model.MealEntry
import com.calovelo.app.domain.model.MealPlanSuggestion
import com.calovelo.app.domain.model.MealType
import com.calovelo.app.domain.nutrition.MacroCalculator
import com.calovelo.app.domain.nutrition.MealSlotPolicy
import com.calovelo.app.domain.nutrition.PlannerConfig
import com.calovelo.app.domain.repository.NutritionRepository
import kotlinx.coroutines.flow.Flow
import java.time.Clock
import java.time.LocalTime
import javax.inject.Inject

class ObserveDailyNutritionUseCase @Inject constructor(
    private val repository: NutritionRepository
) {
    operator fun invoke(): Flow<DailyNutritionSummary> = repository.observeToday()
}

class SearchFoodsUseCase @Inject constructor(
    private val repository: NutritionRepository
) {
    suspend operator fun invoke(
        query: String,
        limit: Int = NutritionRepository.DEFAULT_SEARCH_LIMIT
    ): List<FoodItem> = repository.searchFoods(query, limit)
}

class ScanFoodUseCase @Inject constructor(
    private val repository: NutritionRepository
) {
    suspend operator fun invoke(source: ImageSource): FoodScanResult = repository.scanFood(source)
}

class ScanFoodWithGeminiUseCase @Inject constructor(
    private val repository: NutritionRepository
) {
    suspend operator fun invoke(source: ImageSource): FoodScanResult = repository.scanFoodWithGemini(source)
}

class LogMealsUseCase @Inject constructor(
    private val repository: NutritionRepository
) {
    suspend operator fun invoke(entries: List<MealEntry>, mealType: MealType) {
        require(entries.isNotEmpty()) { "Nothing to log." }
        entries.forEach { entry ->
            require(entry.grams in NutritionRepository.MIN_GRAMS..NutritionRepository.MAX_GRAMS) {
                "Portion for ${entry.food.displayName} must be between ${NutritionRepository.MIN_GRAMS} and " +
                    "${NutritionRepository.MAX_GRAMS} g."
            }
        }
        repository.logMeals(entries, mealType)
    }
}

class MarkPlannedMealEatenUseCase @Inject constructor(
    private val repository: NutritionRepository
) {
    suspend operator fun invoke(mealId: String): Boolean = repository.markPlannedMealEaten(mealId)
}

class DeleteMealUseCase @Inject constructor(
    private val repository: NutritionRepository
) {
    suspend operator fun invoke(mealId: String) = repository.deleteMeal(mealId)
}

class ClearTodayPlanUseCase @Inject constructor(
    private val repository: NutritionRepository
) {
    suspend operator fun invoke() = repository.clearTodayPlan()
}

sealed interface PlanGenerationResult {
    data class Generated(val suggestions: List<MealPlanSuggestion>) : PlanGenerationResult

    /** Today's calorie goal is (almost) met, so there is nothing left to plan. */
    data object GoalAlreadyReached : PlanGenerationResult

    /** The catalog has no foods flagged suitable for the remaining meals. */
    data object NoEligibleFoods : PlanGenerationResult
}

/**
 * "Auto-Generate Today's Diet Plan": works out which meals are still ahead, how many calories and
 * how much protein are left, asks the planner for foods to fill them and stores the result as
 * PLANNED meals (replacing any earlier un-eaten plan).
 */
class GenerateDailyPlanUseCase @Inject constructor(
    private val repository: NutritionRepository,
    private val clock: Clock
) {
    suspend operator fun invoke(config: PlannerConfig = PlannerConfig()): PlanGenerationResult {
        val summary = repository.getTodaySummary()
        val remaining = summary.remaining
        if (remaining.kcal < config.minPlannableKcal) return PlanGenerationResult.GoalAlreadyReached

        val slots = MealSlotPolicy.slotsToPlan(
            now = LocalTime.now(clock),
            loggedTypes = summary.loggedMeals.map { it.mealType }.toSet()
        )
        val plan = MacroCalculator.generateMealPlan(repository.getAllFoods(), remaining, slots, config)
        if (plan.isEmpty()) return PlanGenerationResult.NoEligibleFoods

        repository.replaceTodayPlan(plan)
        return PlanGenerationResult.Generated(plan)
    }
}
