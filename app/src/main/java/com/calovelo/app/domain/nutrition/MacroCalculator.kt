// app/src/main/java/com/calovelo/app/domain/nutrition/MacroCalculator.kt
package com.calovelo.app.domain.nutrition

import com.calovelo.app.domain.model.DailyNutritionSummary
import com.calovelo.app.domain.model.FoodCategory
import com.calovelo.app.domain.model.FoodItem
import com.calovelo.app.domain.model.MacroGoals
import com.calovelo.app.domain.model.Macros
import com.calovelo.app.domain.model.MealLog
import com.calovelo.app.domain.model.MealPlanSuggestion
import com.calovelo.app.domain.model.MealStatus
import com.calovelo.app.domain.model.MealType
import java.time.LocalTime
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** Tunables for [MacroCalculator.generateMealPlan]. The defaults are what the app ships with. */
data class PlannerConfig(
    /** Below this many remaining kcal there is nothing worth planning. */
    val minPlannableKcal: Double = 150.0,
    val minPortionFactor: Double = 0.5,
    val maxPortionFactor: Double = 1.75,
    val gramStep: Int = 5,
    val maxItemsPerMeal: Int = 3,
    val beamWidth: Int = 12,
    /** Added to a combination's score per earlier use of the same food, to keep the day varied. */
    val reusePenalty: Double = 0.12,
    val slotWeights: Map<MealType, Double> = mapOf(
        MealType.BREAKFAST to 0.25,
        MealType.LUNCH to 0.35,
        MealType.DINNER to 0.30,
        MealType.SNACK to 0.10
    )
) {
    init {
        require(gramStep > 0) { "gramStep must be positive" }
        require(maxItemsPerMeal > 0) { "maxItemsPerMeal must be positive" }
        require(beamWidth > 0) { "beamWidth must be positive" }
        require(minPortionFactor > 0.0 && maxPortionFactor >= minPortionFactor) { "invalid portion factors" }
    }

    fun weightOf(type: MealType): Double = slotWeights[type]?.takeIf { it > 0.0 } ?: DEFAULT_SLOT_WEIGHT

    private companion object {
        const val DEFAULT_SLOT_WEIGHT = 0.1
    }
}

/** Decides which meals of the day are still worth planning. */
object MealSlotPolicy {
    private val CUTOFFS: Map<MealType, LocalTime> = mapOf(
        MealType.BREAKFAST to LocalTime.of(11, 0),
        MealType.LUNCH to LocalTime.of(16, 0),
        MealType.DINNER to LocalTime.of(22, 0),
        MealType.SNACK to LocalTime.of(23, 0)
    )

    /**
     * Meals whose time window has not passed and that the user has not already logged. Snacks can
     * be logged repeatedly so they are never excluded by [loggedTypes]. Late at night, with
     * calories still left, a single snack is planned so the user always gets a suggestion.
     */
    fun slotsToPlan(now: LocalTime, loggedTypes: Set<MealType>): List<MealType> {
        val open = MealType.entries.filter { type ->
            (type == MealType.SNACK || type !in loggedTypes) && now.isBefore(CUTOFFS.getValue(type))
        }
        return open.ifEmpty { listOf(MealType.SNACK) }
    }
}

/** Pure, side-effect-free nutrition maths. Everything here is deterministic and unit-tested. */
object MacroCalculator {

    // ---- Scaling & totals -------------------------------------------------------------------

    /** Macros of [grams] of [food], scaled linearly from its per-100 g values. */
    fun macrosFor(food: FoodItem, grams: Double): Macros {
        require(grams.isFinite() && grams >= 0.0) { "grams must be a finite, non-negative number but was $grams" }
        return food.per100g * (grams / 100.0)
    }

    fun total(meals: Iterable<MealLog>): Macros = meals.fold(Macros.ZERO) { acc, meal -> acc + meal.macros }

    /** What is left of [goals] after [consumed]; each nutrient is floored at zero. */
    fun remaining(goals: MacroGoals, consumed: Macros): Macros =
        (goals.toMacros() - consumed).coerceAtLeastZero()

    /** `consumed / goal`, unclamped so callers can detect going over; 0 when there is no goal. */
    fun progress(consumed: Double, goal: Double): Float =
        if (goal <= 0.0) 0f else (consumed / goal).toFloat()

    /** Only LOGGED meals count as consumed; PLANNED meals are reported separately. */
    fun summarize(epochDay: Long, goals: MacroGoals, meals: List<MealLog>): DailyNutritionSummary {
        val consumed = total(meals.filter { it.status == MealStatus.LOGGED })
        val planned = total(meals.filter { it.status == MealStatus.PLANNED })
        return DailyNutritionSummary(
            epochDay = epochDay,
            goals = goals,
            meals = meals,
            consumed = consumed,
            planned = planned,
            remaining = remaining(goals, consumed)
        )
    }

    // ---- Auto meal planner ------------------------------------------------------------------

    /**
     * Suggests foods for each of [slots] so that together they land close to [remaining] calories
     * and protein (carbs and fat are matched more loosely).
     *
     * For each slot, in meal order, the remaining budget is split by [PlannerConfig.slotWeights]
     * across the slots not yet planned. A small beam search then builds the best 1-3 food
     * combination from foods flagged suitable for that meal, with at most one food per category,
     * choosing each portion between [PlannerConfig.minPortionFactor] and
     * [PlannerConfig.maxPortionFactor] times the default serving, in [PlannerConfig.gramStep]
     * increments. Whatever a slot over- or under-shoots rolls into the next slot. Foods already
     * used earlier in the day are penalized to keep the plan varied.
     *
     * Deterministic: identical inputs always yield identical plans.
     */
    fun generateMealPlan(
        foods: List<FoodItem>,
        remaining: Macros,
        slots: List<MealType>,
        config: PlannerConfig = PlannerConfig()
    ): List<MealPlanSuggestion> {
        val orderedSlots = slots.distinct().sortedBy { it.ordinal }
        var left = remaining.coerceAtLeastZero()
        if (orderedSlots.isEmpty() || left.kcal < config.minPlannableKcal) return emptyList()

        val catalog = foods.filter { it.kcalPer100g > 0.0 }.sortedBy { it.labelKey }
        val usage = HashMap<String, Int>()
        val plan = ArrayList<MealPlanSuggestion>()

        for ((index, slot) in orderedSlots.withIndex()) {
            if (left.kcal < config.minPlannableKcal) break

            val weightLeft = orderedSlots.drop(index).sumOf { config.weightOf(it) }
            val target = left * (config.weightOf(slot) / weightLeft)
            val pool = catalog.filter { slot in it.suitableMealTypes }
            val items = planSlot(pool, target, usage, config)
            if (items.isEmpty()) continue

            var slotTotal = Macros.ZERO
            for (item in items.sortedWith(compareByDescending<PlannedItem> { it.macros.kcal }.thenBy { it.food.labelKey })) {
                plan += MealPlanSuggestion(slot, item.food, item.grams, item.macros)
                usage.merge(item.food.labelKey, 1, Int::plus)
                slotTotal += item.macros
            }
            left = (left - slotTotal).coerceAtLeastZero()
        }
        return plan
    }

    // ---- Planner internals ------------------------------------------------------------------

    private const val MULTI_ITEM_KCAL = 300.0
    private const val GRID_POINTS = 11
    private const val POLISH_PASSES = 2
    private val POLISH_DELTAS = intArrayOf(-40, -20, -10, -5, 5, 10, 20, 40)

    private const val W_KCAL = 1.0
    private const val W_PROTEIN = 1.0
    private const val W_CARBS = 0.25
    private const val W_FAT = 0.25
    private const val PROTEIN_OVERSHOOT_FACTOR = 0.5
    private const val KCAL_FLOOR = 100.0
    private const val PROTEIN_FLOOR = 10.0
    private const val CARBS_FLOOR = 20.0
    private const val FAT_FLOOR = 10.0

    private class PlannedItem(val food: FoodItem, val grams: Int, val macros: Macros)

    private class Combo(val items: List<PlannedItem>, val total: Macros, val fit: Double, val reuse: Double) {
        val score: Double get() = fit + reuse
        val key: String = items.map { it.food.labelKey }.sorted().joinToString("|")
    }

    /** Lower is better. Under-shooting protein hurts more than overshooting it. */
    private fun fitScore(total: Macros, target: Macros): Double {
        val kcalErr = abs(total.kcal - target.kcal) / max(target.kcal, KCAL_FLOOR)
        val proteinDiff = total.proteinG - target.proteinG
        val proteinErr = (if (proteinDiff < 0) -proteinDiff else proteinDiff * PROTEIN_OVERSHOOT_FACTOR) /
            max(target.proteinG, PROTEIN_FLOOR)
        val carbsErr = abs(total.carbsG - target.carbsG) / max(target.carbsG, CARBS_FLOOR)
        val fatErr = abs(total.fatG - target.fatG) / max(target.fatG, FAT_FLOOR)
        return W_KCAL * kcalErr + W_PROTEIN * proteinErr + W_CARBS * carbsErr + W_FAT * fatErr
    }

    private fun roundToStep(value: Double, step: Int): Int = (value / step).roundToInt() * step

    private fun gramBounds(food: FoodItem, config: PlannerConfig): IntRange {
        val lo = max(config.gramStep, roundToStep(food.defaultServingGrams * config.minPortionFactor, config.gramStep))
        val hi = max(lo, roundToStep(food.defaultServingGrams * config.maxPortionFactor, config.gramStep))
        return lo..hi
    }

    private fun gramGrid(food: FoodItem, config: PlannerConfig): List<Int> {
        val bounds = gramBounds(food, config)
        return (0 until GRID_POINTS)
            .map { i ->
                val raw = bounds.first + (bounds.last - bounds.first).toDouble() * i / (GRID_POINTS - 1)
                roundToStep(raw, config.gramStep).coerceIn(bounds)
            }
            .distinct()
    }

    private fun planSlot(
        pool: List<FoodItem>,
        target: Macros,
        usage: Map<String, Int>,
        config: PlannerConfig
    ): List<PlannedItem> {
        if (pool.isEmpty()) return emptyList()

        val minItems = if (target.kcal >= MULTI_ITEM_KCAL) 2 else 1
        val maxItems = min(config.maxItemsPerMeal, pool.size)
        val grids = pool.associate { it.labelKey to gramGrid(it, config) }

        var beam = listOf(Combo(emptyList(), Macros.ZERO, 0.0, 0.0))
        var best: Combo? = null

        for (depth in 1..maxItems) {
            val expanded = ArrayList<Combo>()
            val seen = HashSet<String>()
            for (state in beam) {
                val usedCategories = state.items.map { it.food.category }.toSet()
                val usedKeys = state.items.map { it.food.labelKey }.toSet()
                for (food in pool) {
                    if (food.labelKey in usedKeys) continue
                    if (food.category != FoodCategory.OTHER && food.category in usedCategories) continue
                    val combo = bestPortion(state, food, grids.getValue(food.labelKey), target, usage, config)
                    if (seen.add(combo.key)) expanded += combo
                }
            }
            if (expanded.isEmpty()) break

            expanded.sortWith(compareBy<Combo> { it.score }.thenBy { it.key })
            beam = expanded.take(config.beamWidth)

            if (depth >= minItems) {
                val top = beam.first()
                if (best == null || top.score < best.score) best = top
            }
        }

        val chosen = best ?: beam.first().takeIf { it.items.isNotEmpty() } ?: return emptyList()
        return polish(chosen.items, target, config)
    }

    /** Adds [food] to [state] at the portion that gives the lowest score; ties go to the smaller portion. */
    private fun bestPortion(
        state: Combo,
        food: FoodItem,
        grid: List<Int>,
        target: Macros,
        usage: Map<String, Int>,
        config: PlannerConfig
    ): Combo {
        val reuse = state.reuse + (usage[food.labelKey] ?: 0) * config.reusePenalty
        var bestItem: PlannedItem? = null
        var bestTotal = Macros.ZERO
        var bestFit = Double.MAX_VALUE
        for (grams in grid) {
            val macros = macrosFor(food, grams.toDouble())
            val total = state.total + macros
            val fit = fitScore(total, target)
            if (fit < bestFit) {
                bestFit = fit
                bestItem = PlannedItem(food, grams, macros)
                bestTotal = total
            }
        }
        return Combo(state.items + checkNotNull(bestItem), bestTotal, bestFit, reuse)
    }

    /** Coordinate descent: nudge each portion in small steps while that improves the fit. */
    private fun polish(
        items: List<PlannedItem>,
        target: Macros,
        config: PlannerConfig
    ): List<PlannedItem> {
        val current = items.toMutableList()
        var currentFit = fitScore(current.fold(Macros.ZERO) { acc, i -> acc + i.macros }, target)

        repeat(POLISH_PASSES) {
            var improved = false
            for (index in current.indices) {
                val item = current[index]
                val bounds = gramBounds(item.food, config)
                for (delta in POLISH_DELTAS) {
                    val grams = item.grams + delta
                    if (grams !in bounds) continue
                    val candidate = PlannedItem(item.food, grams, macrosFor(item.food, grams.toDouble()))
                    val trial = current.toMutableList().also { it[index] = candidate }
                    val fit = fitScore(trial.fold(Macros.ZERO) { acc, i -> acc + i.macros }, target)
                    if (fit < currentFit - 1e-9) {
                        current[index] = candidate
                        currentFit = fit
                        improved = true
                        break
                    }
                }
            }
            if (!improved) return current
        }
        return current
    }
}
