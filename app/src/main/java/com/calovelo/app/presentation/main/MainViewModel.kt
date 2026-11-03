package com.calovelo.app.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calovelo.app.domain.repository.SettingsRepository
import com.calovelo.app.presentation.theme.CaloThemeId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainUiState(
    val themeId: CaloThemeId = CaloThemeId.EmeraldDark,
    val remindersEnabled: Boolean = false,
    val userName: String = "Athlete",
    val heightCm: Int = 175,
    val weightKg: Float = 70f,
    val dailyCalorieGoal: Int = 2500,
    val dailyWaterGoalMl: Int = 3000,
    val weeklyDistanceGoalKm: Float = 50f,
    val waterIntakeMl: Int = 0
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<MainUiState> = combine(
        settingsRepository.selectedThemeId,
        settingsRepository.hydrationRemindersEnabled,
        settingsRepository.userName,
        settingsRepository.heightCm,
        settingsRepository.weightKg
    ) { themeId, reminders, name, height, weight ->
        // second combine for remaining flows
        MainUiState(
            themeId = themeId,
            remindersEnabled = reminders,
            userName = name,
            heightCm = height,
            weightKg = weight
        )
    }.combine(
        combine(
            settingsRepository.dailyCalorieGoal,
            settingsRepository.dailyWaterGoalMl,
            settingsRepository.weeklyDistanceGoalKm
        ) { cal, water, dist -> Triple(cal, water, dist) }
    ) { base, (cal, water, dist) ->
        base.copy(
            dailyCalorieGoal = cal,
            dailyWaterGoalMl = water,
            weeklyDistanceGoalKm = dist
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MainUiState()
    )

    fun setTheme(themeId: CaloThemeId) {
        viewModelScope.launch { settingsRepository.setThemeId(themeId) }
    }

    fun setReminders(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setHydrationRemindersEnabled(enabled) }
    }

    fun saveProfile(name: String, height: Int, weight: Float) {
        viewModelScope.launch {
            settingsRepository.setUserName(name)
            settingsRepository.setHeightCm(height)
            settingsRepository.setWeightKg(weight)
        }
    }

    fun saveGoals(calories: Int, waterMl: Int, distanceKm: Float) {
        viewModelScope.launch {
            settingsRepository.setDailyCalorieGoal(calories)
            settingsRepository.setDailyWaterGoalMl(waterMl)
            settingsRepository.setWeeklyDistanceGoalKm(distanceKm)
        }
    }
}
