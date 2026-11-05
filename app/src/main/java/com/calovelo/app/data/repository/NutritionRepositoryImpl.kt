// app/src/main/java/com/calovelo/app/data/repository/NutritionRepositoryImpl.kt
package com.calovelo.app.data.repository

import androidx.room.withTransaction
import com.calovelo.app.data.local.CaloveloDatabase
import com.calovelo.app.data.local.dao.FoodCatalogDao
import com.calovelo.app.data.local.dao.HealthSummaryDao
import com.calovelo.app.data.local.dao.MealLogDao
import com.calovelo.app.data.local.entity.DailyHealthSummaryEntity
import com.calovelo.app.data.local.mapper.FoodSearchQuery
import com.calovelo.app.data.local.mapper.toDomain
import com.calovelo.app.data.local.mapper.toLoggedEntity
import com.calovelo.app.data.local.mapper.toPlannedEntity
import com.calovelo.app.data.local.seed.FoodCatalogSeeder
import com.calovelo.app.data.vision.BitmapPreprocessor
import com.calovelo.app.data.vision.TfliteFoodDetector
import com.calovelo.app.domain.model.DailyNutritionSummary
import com.calovelo.app.domain.model.FoodItem
import com.calovelo.app.domain.model.FoodScanResult
import com.calovelo.app.domain.model.ImageSource
import com.calovelo.app.domain.model.MacroGoals
import com.calovelo.app.domain.model.MealEntry
import com.calovelo.app.domain.model.MealPlanSuggestion
import com.calovelo.app.domain.model.MealType
import com.calovelo.app.domain.nutrition.MacroCalculator
import com.calovelo.app.domain.repository.NutritionRepository
import com.calovelo.app.domain.util.todayEpochDayFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NutritionRepositoryImpl @Inject constructor(
    private val database: CaloveloDatabase,
    private val foodCatalogDao: FoodCatalogDao,
    private val mealLogDao: MealLogDao,
    private val summaryDao: HealthSummaryDao,
    private val seeder: FoodCatalogSeeder,
    private val detector: TfliteFoodDetector,
    private val preprocessor: BitmapPreprocessor,
    private val clock: Clock
) : NutritionRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeToday(): Flow<DailyNutritionSummary> =
        todayEpochDayFlow().flatMapLatest { epochDay ->
            // The calorie goal lives on the day's health-summary row (Phase 1); macro goals derive from it.
            val calorieGoal = summaryDao.observeByDay(epochDay)
                .onStart { summaryDao.getOrCreate(epochDay) }
                .map { it?.calorieGoalKcal ?: DailyHealthSummaryEntity.DEFAULT_CALORIE_GOAL_KCAL }
                .distinctUntilChanged()

            combine(mealLogDao.observeForDay(epochDay), calorieGoal) { meals, goalKcal ->
                MacroCalculator.summarize(
                    epochDay = epochDay,
                    goals = MacroGoals.fromCalories(goalKcal),
                    meals = meals.map { it.toDomain() }
                )
            }
        }

    override suspend fun getTodaySummary(): DailyNutritionSummary = observeToday().first()

    override suspend fun getAllFoods(): List<FoodItem> {
        seeder.ensureSeeded()
        return foodCatalogDao.getAll().map { it.toDomain() }
    }

    override suspend fun searchFoods(query: String, limit: Int): List<FoodItem> {
        seeder.ensureSeeded()
        val effectiveLimit = limit.coerceIn(1, MAX_SEARCH_LIMIT)
        return if (query.isBlank()) {
            foodCatalogDao.getAll().take(effectiveLimit).map { it.toDomain() }
        } else {
            val q = FoodSearchQuery.from(query)
            foodCatalogDao
                .search(q.token1, q.token2, q.token3, q.prefixPattern, effectiveLimit)
                .map { it.toDomain() }
        }
    }

    override suspend fun logMeals(entries: List<MealEntry>, mealType: MealType) {
        require(entries.isNotEmpty()) { "Nothing to log." }
        entries.forEach {
            require(it.grams in NutritionRepository.MIN_GRAMS..NutritionRepository.MAX_GRAMS) {
                "Portion must be between ${NutritionRepository.MIN_GRAMS} and ${NutritionRepository.MAX_GRAMS} g."
            }
        }
        val epochDay = LocalDate.now(clock).toEpochDay()
        val now = clock.millis()
        // One @Upsert statement: either every entry is saved or none is.
        mealLogDao.upsertAll(entries.map { it.toLoggedEntity(mealType, epochDay, now) })
    }

    override suspend fun replaceTodayPlan(suggestions: List<MealPlanSuggestion>) {
        val epochDay = LocalDate.now(clock).toEpochDay()
        val now = clock.millis()
        database.withTransaction {
            mealLogDao.deleteAutoPlannedForDay(epochDay)
            mealLogDao.upsertAll(suggestions.map { it.toPlannedEntity(epochDay, now) })
        }
    }

    override suspend fun clearTodayPlan() {
        mealLogDao.deleteAutoPlannedForDay(LocalDate.now(clock).toEpochDay())
    }

    override suspend fun markPlannedMealEaten(mealId: String): Boolean =
        mealLogDao.markEaten(mealId, clock.millis()) > 0

    override suspend fun deleteMeal(mealId: String) = mealLogDao.deleteById(mealId)

    override suspend fun scanFood(source: ImageSource): FoodScanResult {
        // Cheap early exit: no point decoding a photo when there is no model to run on it.
        detector.unavailableReason()?.let { return FoodScanResult.ModelUnavailable(it) }

        val bitmap = withContext(Dispatchers.IO) { preprocessor.decodeUpright({ source.open() }) }
            ?: return FoodScanResult.Failure("Couldn't read that image. Try another photo.")
        return try {
            detector.detect(bitmap)
        } finally {
            bitmap.recycle()
        }
    }

    private companion object {
        const val MAX_SEARCH_LIMIT = 100
    }
}
