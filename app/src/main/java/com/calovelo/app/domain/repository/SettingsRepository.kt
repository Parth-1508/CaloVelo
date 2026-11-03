package com.calovelo.app.domain.repository

import com.calovelo.app.presentation.theme.CaloThemeId
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val hydrationRemindersEnabled: Flow<Boolean>
    suspend fun setHydrationRemindersEnabled(enabled: Boolean)

    val selectedThemeId: Flow<CaloThemeId>
    suspend fun setThemeId(themeId: CaloThemeId)

    val userName: Flow<String>
    suspend fun setUserName(name: String)

    val dailyCalorieGoal: Flow<Int>
    suspend fun setDailyCalorieGoal(kcal: Int)

    val dailyWaterGoalMl: Flow<Int>
    suspend fun setDailyWaterGoalMl(ml: Int)

    val weeklyDistanceGoalKm: Flow<Float>
    suspend fun setWeeklyDistanceGoalKm(km: Float)

    val heightCm: Flow<Int>
    suspend fun setHeightCm(cm: Int)

    val weightKg: Flow<Float>
    suspend fun setWeightKg(kg: Float)
}
