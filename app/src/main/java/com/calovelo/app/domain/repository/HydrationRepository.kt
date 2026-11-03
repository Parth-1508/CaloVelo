package com.calovelo.app.domain.repository

interface HydrationRepository {
    /** Persists a water log and updates today's intake atomically. */
    suspend fun logWater(amountMl: Int)

    companion object {
        const val MIN_LOG_ML = 1
        const val MAX_LOG_ML = 5_000
    }
}
