package com.calovelo.app.domain.usecase

import com.calovelo.app.domain.model.DailyHealthSummary
import com.calovelo.app.domain.model.SyncResult
import com.calovelo.app.domain.model.WearableAccess
import com.calovelo.app.domain.repository.HealthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveTodaySummaryUseCase @Inject constructor(
    private val repository: HealthRepository
) {
    operator fun invoke(): Flow<DailyHealthSummary> = repository.observeToday()
}

class CheckWearableAccessUseCase @Inject constructor(
    private val repository: HealthRepository
) {
    suspend operator fun invoke(): WearableAccess = repository.checkWearableAccess()
}

class SyncWearablesUseCase @Inject constructor(
    private val repository: HealthRepository
) {
    suspend operator fun invoke(): SyncResult = repository.syncWearables()
}
