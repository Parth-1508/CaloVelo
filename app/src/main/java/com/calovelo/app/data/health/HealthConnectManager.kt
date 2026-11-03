package com.calovelo.app.data.health

import android.content.Context
import android.os.Build
import androidx.activity.result.contract.ActivityResultContract
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.calovelo.app.domain.model.HealthStats
import com.calovelo.app.domain.model.WearableAccess
import dagger.Lazy
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single entry point to Health Connect.
 *
 * The client is injected as [Lazy] because HealthConnectClient.getOrCreate() throws on
 * devices where the provider is missing; it is only resolved after the SDK status check.
 */
@Singleton
class HealthConnectManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val clientProvider: Lazy<HealthConnectClient>
) {

    val requiredPermissions: Set<String> = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class)
    )

    fun createPermissionContract(): ActivityResultContract<Set<String>, Set<String>> =
        PermissionController.createRequestPermissionResultContract()

    fun isAvailable(): Boolean =
        HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE

    suspend fun hasAllPermissions(): Boolean = try {
        clientProvider.get().permissionController.getGrantedPermissions()
            .containsAll(requiredPermissions)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        false
    }

    suspend fun checkAccess(): WearableAccess = when (HealthConnectClient.getSdkStatus(context)) {
        HealthConnectClient.SDK_AVAILABLE ->
            if (hasAllPermissions()) WearableAccess.READY else WearableAccess.PERMISSION_REQUIRED

        HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED ->
            WearableAccess.PROVIDER_INSTALL_OR_UPDATE

        // Before Android 14, Health Connect is a separate app the user can install.
        else ->
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                WearableAccess.PROVIDER_INSTALL_OR_UPDATE
            } else {
                WearableAccess.UNAVAILABLE
            }
    }

    /**
     * Aggregates today's (local midnight until now) steps, active calories and average heart
     * rate. Sleep is aggregated from 18:00 yesterday so last night's session is included.
     *
     * @throws IllegalStateException if Health Connect is unavailable.
     * @throws SecurityException if a required permission has not been granted.
     */
    suspend fun aggregateTodayStats(zone: ZoneId = ZoneId.systemDefault()): HealthStats {
        check(isAvailable()) { "Health Connect is not available on this device." }
        val client = clientProvider.get()

        val now = ZonedDateTime.now(zone)
        val today = now.toLocalDate()
        val dayStart = today.atStartOfDay(zone).toInstant()
        val rangeEnd = maxOf(now.toInstant(), dayStart.plusSeconds(1))

        val daily = client.aggregate(
            AggregateRequest(
                metrics = setOf(
                    StepsRecord.COUNT_TOTAL,
                    ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL,
                    HeartRateRecord.BPM_AVG
                ),
                timeRangeFilter = TimeRangeFilter.between(dayStart, rangeEnd)
            )
        )

        val sleepStart = today.minusDays(1).atTime(18, 0).atZone(zone).toInstant()
        val sleep = client.aggregate(
            AggregateRequest(
                metrics = setOf(SleepSessionRecord.SLEEP_DURATION_TOTAL),
                timeRangeFilter = TimeRangeFilter.between(sleepStart, rangeEnd)
            )
        )

        return HealthStats(
            epochDay = today.toEpochDay(),
            steps = daily[StepsRecord.COUNT_TOTAL] ?: 0L,
            activeCaloriesKcal = daily[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]?.inKilocalories ?: 0.0,
            averageHeartRateBpm = daily[HeartRateRecord.BPM_AVG]?.toInt(),
            sleepMinutes = sleep[SleepSessionRecord.SLEEP_DURATION_TOTAL]?.toMinutes() ?: 0L
        )
    }
}
