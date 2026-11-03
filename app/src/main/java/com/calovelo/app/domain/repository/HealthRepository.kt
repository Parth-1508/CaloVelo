package com.calovelo.app.domain.repository

import androidx.activity.result.contract.ActivityResultContract
import com.calovelo.app.domain.model.DailyHealthSummary
import com.calovelo.app.domain.model.SyncResult
import com.calovelo.app.domain.model.WearableAccess
import kotlinx.coroutines.flow.Flow

interface HealthRepository {
    val requiredPermissions: Set<String>

    fun permissionRequestContract(): ActivityResultContract<Set<String>, Set<String>>

    /** Emits today's summary (creating the row if needed) and rolls over at midnight. */
    fun observeToday(): Flow<DailyHealthSummary>

    suspend fun getToday(): DailyHealthSummary

    suspend fun checkWearableAccess(): WearableAccess

    /** Pulls today's aggregates from Health Connect and persists them to Room. */
    suspend fun syncWearables(): SyncResult
}
