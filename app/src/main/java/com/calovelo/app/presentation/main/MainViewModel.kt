package com.calovelo.app.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calovelo.app.domain.model.DietaryFilter
import com.calovelo.app.domain.repository.SettingsRepository
import com.calovelo.app.domain.usecase.LogWaterUseCase
import com.calovelo.app.domain.usecase.ObserveTodaySummaryUseCase
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
    val waterIntakeMl: Int = 0,
    val avatarUri: String? = null,
    val dietaryFilter: DietaryFilter = DietaryFilter.ALL,
    val ageYears: Int = 25,
    val targetWeightKg: Float = 70f,
    val bioText: String = "",
    val locationString: String = ""
)

private data class GoalsAndProfile(
    val cal: Int,
    val water: Int,
    val dist: Float,
    val avatar: String?,
    val diet: DietaryFilter,
    val age: Int,
    val targetWeight: Float,
    val bio: String,
    val location: String
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val logWaterUseCase: LogWaterUseCase,
    private val observeTodaySummaryUseCase: ObserveTodaySummaryUseCase
) : ViewModel() {

    val uiState: StateFlow<MainUiState> = combine(
        settingsRepository.selectedThemeId,
        settingsRepository.hydrationRemindersEnabled,
        settingsRepository.userName,
        settingsRepository.heightCm,
        settingsRepository.weightKg
    ) { themeId, reminders, name, height, weight ->
        MainUiState(
            themeId = themeId,
            remindersEnabled = reminders,
            userName = name,
            heightCm = height,
            weightKg = weight
        )
    }.combine(
        combine(
            combine(
                settingsRepository.dailyCalorieGoal,
                settingsRepository.dailyWaterGoalMl,
                settingsRepository.weeklyDistanceGoalKm,
                settingsRepository.avatarUri,
                settingsRepository.dietaryFilter
            ) { cal, water, dist, avatar, diet ->
                Tuple5(cal, water, dist, avatar, diet)
            },
            combine(
                settingsRepository.ageYears,
                settingsRepository.targetWeightKg,
                settingsRepository.bioText,
                settingsRepository.locationString
            ) { age, targetWeight, bio, location ->
                Tuple4(age, targetWeight, bio, location)
            }
        ) { t5, t4 ->
            GoalsAndProfile(
                cal = t5.v1,
                water = t5.v2,
                dist = t5.v3,
                avatar = t5.v4,
                diet = t5.v5,
                age = t4.v1,
                targetWeight = t4.v2,
                bio = t4.v3,
                location = t4.v4
            )
        }
    ) { base, gp ->
        base.copy(
            dailyCalorieGoal = gp.cal,
            dailyWaterGoalMl = gp.water,
            weeklyDistanceGoalKm = gp.dist,
            avatarUri = gp.avatar,
            dietaryFilter = gp.diet,
            ageYears = gp.age,
            targetWeightKg = gp.targetWeight,
            bioText = gp.bio,
            locationString = gp.location
        )
    }.combine(
        observeTodaySummaryUseCase()
    ) { state, summary ->
        state.copy(
            waterIntakeMl = summary.waterIntakeMl
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

    fun setDietaryFilter(filter: DietaryFilter) {
        viewModelScope.launch { settingsRepository.setDietaryFilter(filter) }
    }

    fun setAvatarUri(uri: String?) {
        viewModelScope.launch { settingsRepository.setAvatarUri(uri) }
    }

    fun setAge(age: Int) {
        viewModelScope.launch { settingsRepository.setAgeYears(age) }
    }

    fun setTargetWeight(weight: Float) {
        viewModelScope.launch { settingsRepository.setTargetWeightKg(weight) }
    }

    fun setBio(bio: String) {
        viewModelScope.launch { settingsRepository.setBioText(bio) }
    }

    fun setLocation(location: String) {
        viewModelScope.launch { settingsRepository.setLocationString(location) }
    }

    fun quickAddWater(amountMl: Int) {
        viewModelScope.launch {
            logWaterUseCase(amountMl)
        }
    }
}

private data class Tuple4<A, B, C, D>(val v1: A, val v2: B, val v3: C, val v4: D)
private data class Tuple5<A, B, C, D, E>(val v1: A, val v2: B, val v3: C, val v4: D, val v5: E)
