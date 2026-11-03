package com.calovelo.app.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calovelo.app.domain.model.SyncResult
import com.calovelo.app.domain.model.WearableAccess
import com.calovelo.app.domain.repository.HealthRepository
import com.calovelo.app.domain.usecase.CheckWearableAccessUseCase
import com.calovelo.app.domain.usecase.LogWaterUseCase
import com.calovelo.app.domain.usecase.ObserveHydrationRemindersUseCase
import com.calovelo.app.domain.usecase.ObserveTodaySummaryUseCase
import com.calovelo.app.domain.usecase.SetHydrationRemindersUseCase
import com.calovelo.app.domain.usecase.SyncWearablesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class DashboardViewModel @Inject constructor(
    observeTodaySummary: ObserveTodaySummaryUseCase,
    observeHydrationReminders: ObserveHydrationRemindersUseCase,
    private val checkWearableAccess: CheckWearableAccessUseCase,
    private val syncWearables: SyncWearablesUseCase,
    private val logWater: LogWaterUseCase,
    private val setHydrationReminders: SetHydrationRemindersUseCase,
    healthRepository: HealthRepository
) : ViewModel() {

    val requiredPermissions: Set<String> = healthRepository.requiredPermissions
    val permissionContract = healthRepository.permissionRequestContract()

    private val access = MutableStateFlow<WearableAccess?>(null)
    private val isSyncing = MutableStateFlow(false)
    private val userMessage = MutableStateFlow<String?>(null)

    /** Single source of truth for the dashboard: Room summary + settings + transient sync state. */
    val uiState: StateFlow<DashboardUiState> = combine(
        observeTodaySummary(),
        observeHydrationReminders(),
        access,
        isSyncing,
        userMessage
    ) { summary, remindersEnabled, wearableAccess, syncing, message ->
        DashboardUiState(
            isLoading = false,
            wearable = WearableCardState(
                steps = summary.steps,
                activeCaloriesKcal = summary.activeCaloriesBurned.roundToInt(),
                heartRateBpm = summary.restingHeartRate,
                lastSyncedTimestamp = summary.lastSyncedTimestamp,
                access = wearableAccess,
                isSyncing = syncing
            ),
            hydration = HydrationCardState(
                intakeMl = summary.waterIntakeMl,
                goalMl = summary.waterGoalMl,
                progress = summary.hydrationProgress,
                percent = if (summary.waterGoalMl > 0) {
                    (summary.waterIntakeMl * 100f / summary.waterGoalMl).roundToInt()
                } else {
                    0
                },
                remindersEnabled = remindersEnabled
            ),
            userMessage = message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState()
    )

    /** Called on every screen start so returning from Health Connect settings picks up changes. */
    fun onScreenStarted() {
        viewModelScope.launch { refreshAccessAndSync() }
    }

    fun onSyncClicked() {
        viewModelScope.launch { performSync() }
    }

    fun onPermissionsResult(granted: Set<String>) {
        viewModelScope.launch {
            refreshAccessAndSync()
            if (access.value == WearableAccess.PERMISSION_REQUIRED && !granted.containsAll(requiredPermissions)) {
                userMessage.value = "Some Health Connect permissions are still missing. " +
                    "You can grant them in Health Connect settings."
            }
        }
    }

    fun onQuickAddWater(amountMl: Int) {
        viewModelScope.launch {
            try {
                logWater(amountMl)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                userMessage.value = "Couldn't save your water entry. Please try again."
            }
        }
    }

    fun onRemindersToggled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                setHydrationReminders(enabled)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                userMessage.value = "Couldn't update reminder settings."
            }
        }
    }

    fun onNotificationPermissionResult(granted: Boolean) {
        if (granted) {
            onRemindersToggled(true)
        } else {
            userMessage.value = "Allow notifications in system settings to receive hydration reminders."
        }
    }

    fun onMessageShown() {
        userMessage.value = null
    }

    private suspend fun refreshAccessAndSync() {
        val current = checkWearableAccess()
        access.value = current
        if (current == WearableAccess.READY) performSync()
    }

    private suspend fun performSync() {
        if (!isSyncing.compareAndSet(expect = false, update = true)) return
        try {
            when (val result = syncWearables()) {
                is SyncResult.Success -> access.value = WearableAccess.READY
                is SyncResult.Blocked -> access.value = result.access
                is SyncResult.Failure -> userMessage.value = result.message
            }
        } finally {
            isSyncing.value = false
        }
    }
}
