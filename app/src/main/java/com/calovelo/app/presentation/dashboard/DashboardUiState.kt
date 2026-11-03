package com.calovelo.app.presentation.dashboard

import com.calovelo.app.domain.model.WearableAccess

data class WearableCardState(
    val steps: Long = 0L,
    val activeCaloriesKcal: Int = 0,
    val heartRateBpm: Int? = null,
    val lastSyncedTimestamp: Long = 0L,
    val access: WearableAccess? = null,
    val isSyncing: Boolean = false
)

data class HydrationCardState(
    val intakeMl: Int = 0,
    val goalMl: Int = 0,
    /** 0f..1f, clamped. */
    val progress: Float = 0f,
    /** Unclamped percentage; can exceed 100. */
    val percent: Int = 0,
    val remindersEnabled: Boolean = false
)

data class DashboardUiState(
    val isLoading: Boolean = true,
    val wearable: WearableCardState = WearableCardState(),
    val hydration: HydrationCardState = HydrationCardState(),
    val userMessage: String? = null
)
