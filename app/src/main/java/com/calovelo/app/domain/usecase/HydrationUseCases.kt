package com.calovelo.app.domain.usecase

import com.calovelo.app.domain.repository.HydrationRepository
import com.calovelo.app.domain.repository.ReminderScheduler
import com.calovelo.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LogWaterUseCase @Inject constructor(
    private val repository: HydrationRepository
) {
    suspend operator fun invoke(amountMl: Int) = repository.logWater(amountMl)
}

class ObserveHydrationRemindersUseCase @Inject constructor(
    private val settings: SettingsRepository
) {
    operator fun invoke(): Flow<Boolean> = settings.hydrationRemindersEnabled
}

class SetHydrationRemindersUseCase @Inject constructor(
    private val settings: SettingsRepository,
    private val scheduler: ReminderScheduler
) {
    suspend operator fun invoke(enabled: Boolean) {
        settings.setHydrationRemindersEnabled(enabled)
        if (enabled) scheduler.scheduleHydrationReminders() else scheduler.cancelHydrationReminders()
    }
}
