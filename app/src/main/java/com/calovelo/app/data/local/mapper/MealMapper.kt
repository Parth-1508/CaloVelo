// app/src/main/java/com/calovelo/app/data/local/mapper/MealMapper.kt
package com.calovelo.app.data.local.mapper

import com.calovelo.app.data.local.entity.MealLogEntity
import com.calovelo.app.domain.model.Macros
import com.calovelo.app.domain.model.MealEntry
import com.calovelo.app.domain.model.MealLog
import com.calovelo.app.domain.model.MealPlanSuggestion
import com.calovelo.app.domain.model.MealSource
import com.calovelo.app.domain.model.MealStatus
import com.calovelo.app.domain.model.MealType
import com.calovelo.app.domain.nutrition.MacroCalculator
import java.util.UUID

fun MealLogEntity.toDomain(): MealLog = MealLog(
    id = id,
    foodLabelKey = foodLabelKey,
    foodName = foodName,
    mealType = MealType.fromName(mealType) ?: MealType.SNACK,
    status = MealStatus.fromName(status),
    source = MealSource.fromName(source),
    epochDay = dateEpochDay,
    timestampMillis = timestamp,
    grams = grams,
    macros = Macros(kcal, proteinG, carbsG, fatG, fiberG)
)

/** An eaten meal: macros are computed from the catalog entry and frozen into the row. */
fun MealEntry.toLoggedEntity(mealType: MealType, epochDay: Long, nowMillis: Long): MealLogEntity {
    val macros = MacroCalculator.macrosFor(food, grams.toDouble())
    return MealLogEntity(
        id = UUID.randomUUID().toString(),
        foodLabelKey = food.labelKey,
        foodName = food.displayName,
        mealType = mealType.name,
        status = MealLogEntity.STATUS_LOGGED,
        source = source.name,
        dateEpochDay = epochDay,
        timestamp = nowMillis,
        grams = grams.toDouble(),
        kcal = macros.kcal,
        proteinG = macros.proteinG,
        carbsG = macros.carbsG,
        fatG = macros.fatG,
        fiberG = macros.fiberG
    )
}

/** A meal the auto-planner proposes, stored as PLANNED until the user ticks it off. */
fun MealPlanSuggestion.toPlannedEntity(epochDay: Long, nowMillis: Long): MealLogEntity = MealLogEntity(
    id = UUID.randomUUID().toString(),
    foodLabelKey = food.labelKey,
    foodName = food.displayName,
    mealType = mealType.name,
    status = MealLogEntity.STATUS_PLANNED,
    source = MealLogEntity.SOURCE_AUTO_PLAN,
    dateEpochDay = epochDay,
    timestamp = nowMillis,
    grams = grams.toDouble(),
    kcal = macros.kcal,
    proteinG = macros.proteinG,
    carbsG = macros.carbsG,
    fatG = macros.fatG,
    fiberG = macros.fiberG
)
