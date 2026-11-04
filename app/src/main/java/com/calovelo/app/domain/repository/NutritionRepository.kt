// app/src/main/java/com/calovelo/app/domain/repository/NutritionRepository.kt
package com.calovelo.app.domain.repository

import com.calovelo.app.domain.model.DailyNutritionSummary
import com.calovelo.app.domain.model.FoodItem
import com.calovelo.app.domain.model.FoodScanResult
import com.calovelo.app.domain.model.ImageSource
import com.calovelo.app.domain.model.MealEntry
import com.calovelo.app.domain.model.MealPlanSuggestion
import com.calovelo.app.domain.model.MealType
import kotlinx.coroutines.flow.Flow

interface NutritionRepository {

    /** Today's meals, goals and totals; re-emits on every change and when midnight passes. */
    fun observeToday(): Flow<DailyNutritionSummary>

    suspend fun getTodaySummary(): DailyNutritionSummary

    suspend fun getAllFoods(): List<FoodItem>

    /** Case-insensitive match on food name or category; blank returns the catalog alphabetically. */
    suspend fun searchFoods(query: String, limit: Int = DEFAULT_SEARCH_LIMIT): List<FoodItem>

    /** Saves [entries] as eaten (LOGGED) meals for today, atomically. */
    suspend fun logMeals(entries: List<MealEntry>, mealType: MealType)

    /** Replaces today's not-yet-eaten auto-generated plan with [suggestions]. Eaten meals are untouched. */
    suspend fun replaceTodayPlan(suggestions: List<MealPlanSuggestion>)

    suspend fun clearTodayPlan()

    /** Turns a PLANNED meal into a LOGGED one. Returns false if it no longer exists or was already eaten. */
    suspend fun markPlannedMealEaten(mealId: String): Boolean

    suspend fun deleteMeal(mealId: String)

    /**
     * Runs on-device food detection on the photo. Never throws for expected problems: a missing
     * model, an unreadable image or an empty result all come back as a [FoodScanResult].
     */
    suspend fun scanFood(source: ImageSource): FoodScanResult

    companion object {
        const val MIN_GRAMS = 1
        const val MAX_GRAMS = 5_000
        const val DEFAULT_SEARCH_LIMIT = 30
    }
}
