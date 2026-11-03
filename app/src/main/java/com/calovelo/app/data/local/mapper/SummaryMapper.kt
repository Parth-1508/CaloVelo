package com.calovelo.app.data.local.mapper

import com.calovelo.app.data.local.entity.DailyHealthSummaryEntity
import com.calovelo.app.domain.model.DailyHealthSummary

fun DailyHealthSummaryEntity.toDomain(): DailyHealthSummary = DailyHealthSummary(
    epochDay = dateEpochDay,
    steps = steps,
    activeCaloriesBurned = activeCaloriesBurned,
    restingHeartRate = restingHeartRate,
    sleepMinutes = sleepMinutes,
    waterIntakeMl = waterIntakeMl,
    waterGoalMl = waterGoalMl,
    calorieGoalKcal = calorieGoalKcal,
    lastSyncedTimestamp = lastSyncedTimestamp
)
