package com.calovelo.app.data.repository

import androidx.room.withTransaction
import com.calovelo.app.data.local.CaloveloDatabase
import com.calovelo.app.data.local.dao.HealthSummaryDao
import com.calovelo.app.data.local.dao.WaterLogDao
import com.calovelo.app.data.local.entity.WaterLogEntity
import com.calovelo.app.domain.repository.HydrationRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HydrationRepositoryImpl @Inject constructor(
    private val database: CaloveloDatabase,
    private val summaryDao: HealthSummaryDao,
    private val waterLogDao: WaterLogDao
) : HydrationRepository {

    override suspend fun logWater(amountMl: Int) {
        require(amountMl in HydrationRepository.MIN_LOG_ML..HydrationRepository.MAX_LOG_ML) {
            "Water amount must be between ${HydrationRepository.MIN_LOG_ML} and " +
                "${HydrationRepository.MAX_LOG_ML} ml."
        }

        val zone = ZoneId.systemDefault()
        val now = ZonedDateTime.now(zone)
        val today = now.toLocalDate()
        val epochDay = today.toEpochDay()
        val dayStartMillis = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val dayEndMillis = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()

        database.withTransaction {
            waterLogDao.upsert(
                WaterLogEntity(timestamp = now.toInstant().toEpochMilli(), amountMl = amountMl)
            )
            summaryDao.getOrCreate(epochDay)
            // Recompute from the log so the summary can never drift from the source of truth.
            summaryDao.updateWaterIntake(epochDay, waterLogDao.getTotalBetween(dayStartMillis, dayEndMillis))
        }
    }

    override fun observeTodayLogs(): Flow<List<WaterLogEntity>> {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val dayStartMillis = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val dayEndMillis = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return waterLogDao.observeBetween(dayStartMillis, dayEndMillis)
    }
}
