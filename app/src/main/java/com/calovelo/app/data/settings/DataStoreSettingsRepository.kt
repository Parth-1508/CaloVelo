package com.calovelo.app.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.calovelo.app.domain.repository.SettingsRepository
import com.calovelo.app.presentation.theme.CaloThemeId
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "calovelo_settings")

@Singleton
class DataStoreSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    private val remindersKey = booleanPreferencesKey("hydration_reminders_enabled")
    private val themeKey = stringPreferencesKey("selected_theme_id")
    private val userNameKey = stringPreferencesKey("user_name")
    private val calorieGoalKey = intPreferencesKey("daily_calorie_goal")
    private val waterGoalKey = intPreferencesKey("daily_water_goal_ml")
    private val distanceGoalKey = floatPreferencesKey("weekly_distance_goal_km")
    private val heightKey = intPreferencesKey("height_cm")
    private val weightKey = floatPreferencesKey("weight_kg")

    private fun prefsFlow(): Flow<Preferences> = context.settingsDataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }

    override val hydrationRemindersEnabled: Flow<Boolean> = prefsFlow()
        .map { prefs -> prefs[remindersKey] ?: false }
        .distinctUntilChanged()

    override suspend fun setHydrationRemindersEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[remindersKey] = enabled }
    }

    override val selectedThemeId: Flow<CaloThemeId> = prefsFlow()
        .map { prefs ->
            try {
                CaloThemeId.valueOf(prefs[themeKey] ?: CaloThemeId.EmeraldDark.name)
            } catch (_: IllegalArgumentException) {
                CaloThemeId.EmeraldDark
            }
        }
        .distinctUntilChanged()

    override suspend fun setThemeId(themeId: CaloThemeId) {
        context.settingsDataStore.edit { it[themeKey] = themeId.name }
    }

    override val userName: Flow<String> = prefsFlow()
        .map { prefs -> prefs[userNameKey] ?: "Athlete" }
        .distinctUntilChanged()

    override suspend fun setUserName(name: String) {
        context.settingsDataStore.edit { it[userNameKey] = name }
    }

    override val dailyCalorieGoal: Flow<Int> = prefsFlow()
        .map { prefs -> prefs[calorieGoalKey] ?: 2500 }
        .distinctUntilChanged()

    override suspend fun setDailyCalorieGoal(kcal: Int) {
        context.settingsDataStore.edit { it[calorieGoalKey] = kcal }
    }

    override val dailyWaterGoalMl: Flow<Int> = prefsFlow()
        .map { prefs -> prefs[waterGoalKey] ?: 3000 }
        .distinctUntilChanged()

    override suspend fun setDailyWaterGoalMl(ml: Int) {
        context.settingsDataStore.edit { it[waterGoalKey] = ml }
    }

    override val weeklyDistanceGoalKm: Flow<Float> = prefsFlow()
        .map { prefs -> prefs[distanceGoalKey] ?: 50f }
        .distinctUntilChanged()

    override suspend fun setWeeklyDistanceGoalKm(km: Float) {
        context.settingsDataStore.edit { it[distanceGoalKey] = km }
    }

    override val heightCm: Flow<Int> = prefsFlow()
        .map { prefs -> prefs[heightKey] ?: 175 }
        .distinctUntilChanged()

    override suspend fun setHeightCm(cm: Int) {
        context.settingsDataStore.edit { it[heightKey] = cm }
    }

    override val weightKg: Flow<Float> = prefsFlow()
        .map { prefs -> prefs[weightKey] ?: 70f }
        .distinctUntilChanged()

    override suspend fun setWeightKg(kg: Float) {
        context.settingsDataStore.edit { it[weightKey] = kg }
    }
}
