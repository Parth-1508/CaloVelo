package com.calovelo.app.presentation.hydration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calovelo.app.data.local.entity.WaterLogEntity
import com.calovelo.app.domain.usecase.LogWaterUseCase
import com.calovelo.app.domain.usecase.ObserveTodaySummaryUseCase
import com.calovelo.app.domain.usecase.ObserveTodayWaterLogsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HydrationUiState(
    val intakeMl: Int = 0,
    val goalMl: Int = 3000,
    val progress: Float = 0f,
    val percent: Int = 0,
    val recentLogs: List<WaterLogEntity> = emptyList()
)

@HiltViewModel
class HydrationViewModel @Inject constructor(
    observeTodaySummary: ObserveTodaySummaryUseCase,
    observeTodayWaterLogs: ObserveTodayWaterLogsUseCase,
    private val logWater: LogWaterUseCase
) : ViewModel() {

    val uiState: StateFlow<HydrationUiState> = combine(
        observeTodaySummary(),
        observeTodayWaterLogs()
    ) { summary, logs ->
        HydrationUiState(
            intakeMl = summary.waterIntakeMl,
            goalMl = summary.waterGoalMl,
            progress = summary.hydrationProgress,
            percent = if (summary.waterGoalMl > 0) (summary.waterIntakeMl * 100 / summary.waterGoalMl) else 0,
            recentLogs = logs
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HydrationUiState()
    )

    fun quickAddWater(amountMl: Int) {
        viewModelScope.launch {
            logWater(amountMl)
        }
    }
}
