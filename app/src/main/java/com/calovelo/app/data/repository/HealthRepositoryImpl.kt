package com.calovelo.app.data.repository

import androidx.activity.result.contract.ActivityResultContract
import com.calovelo.app.data.health.HealthConnectManager
import com.calovelo.app.data.local.dao.HealthSummaryDao
import com.calovelo.app.data.local.mapper.toDomain
import com.calovelo.app.domain.model.DailyHealthSummary
import com.calovelo.app.domain.model.SyncResult
import com.calovelo.app.domain.model.WearableAccess
import com.calovelo.app.domain.repository.HealthRepository
import com.calovelo.app.domain.util.todayEpochDayFlow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import java.io.IOException
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthRepositoryImpl @Inject constructor(
    private val summaryDao: HealthSummaryDao,
    private val healthConnectManager: HealthConnectManager
) : HealthRepository {

    override val requiredPermissions: Set<String>
        get() = healthConnectManager.requiredPermissions

    override fun permissionRequestContract(): ActivityResultContract<Set<String>, Set<String>> =
        healthConnectManager.createPermissionContract()

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeToday(): Flow<DailyHealthSummary> =
        todayEpochDayFlow().flatMapLatest { epochDay ->
            summaryDao.observeByDay(epochDay)
                .onStart { summaryDao.getOrCreate(epochDay) }
                .filterNotNull()
                .map { it.toDomain() }
        }

    override suspend fun getToday(): DailyHealthSummary =
        summaryDao.getOrCreate(LocalDate.now(ZoneId.systemDefault()).toEpochDay()).toDomain()

    override suspend fun checkWearableAccess(): WearableAccess = healthConnectManager.checkAccess()

    override suspend fun syncWearables(): SyncResult {
        val access = healthConnectManager.checkAccess()
        if (access != WearableAccess.READY) return SyncResult.Blocked(access)

        return try {
            val stats = healthConnectManager.aggregateTodayStats()
            summaryDao.applyWearableStats(
                epochDay = stats.epochDay,
                steps = stats.steps,
                activeCalories = stats.activeCaloriesKcal,
                restingHeartRate = stats.averageHeartRateBpm,
                sleepMinutes = stats.sleepMinutes,
                syncedAt = System.currentTimeMillis()
            )
            SyncResult.Success(stats)
        } catch (e: CancellationException) {
            throw e
        } catch (e: SecurityException) {
            SyncResult.Blocked(WearableAccess.PERMISSION_REQUIRED)
        } catch (e: IOException) {
            SyncResult.Failure("Couldn't reach Health Connect. Please try again.")
        } catch (e: Exception) {
            SyncResult.Failure("Sync failed: ${e.localizedMessage ?: "unknown error"}")
        }
    }
}
