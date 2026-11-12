package com.calovelo.app.domain.repository

import com.calovelo.app.data.local.entity.WaterLogEntity
import kotlinx.coroutines.flow.Flow

interface HydrationRepository {
    /** Persists a water log and updates today's intake atomically. */
    suspend fun logWater(amountMl: Int)

    /** Observes today's water log entries in reverse chronological order. */
    fun observeTodayLogs(): Flow<List<WaterLogEntity>>

    companion object {
        const val MIN_LOG_ML = 1
        const val MAX_LOG_ML = 5_000
    }
}
