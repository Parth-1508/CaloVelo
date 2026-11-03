package com.calovelo.app.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val hydrationRemindersEnabled: Flow<Boolean>
    suspend fun setHydrationRemindersEnabled(enabled: Boolean)
}
