package com.calovelo.app.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.calovelo.app.domain.repository.SettingsRepository
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

    // Opt-in: reminders stay off until the user enables them (and grants notification permission).
    override val hydrationRemindersEnabled: Flow<Boolean> = context.settingsDataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> prefs[remindersKey] ?: false }
        .distinctUntilChanged()

    override suspend fun setHydrationRemindersEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[remindersKey] = enabled }
    }
}
