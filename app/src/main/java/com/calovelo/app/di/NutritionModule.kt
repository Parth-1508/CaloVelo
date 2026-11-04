// app/src/main/java/com/calovelo/app/di/NutritionModule.kt
package com.calovelo.app.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NutritionModule {

    /**
     * Wall clock that follows the device time zone as it changes. `Clock.systemDefaultZone()` would
     * freeze the zone at process start, so a user flying across zones would see yesterday's meals.
     */
    @Provides
    @Singleton
    fun provideClock(): Clock = DeviceZoneClock

    private object DeviceZoneClock : Clock() {
        override fun getZone(): ZoneId = ZoneId.systemDefault()
        override fun withZone(zone: ZoneId): Clock = systemUTC().withZone(zone)
        override fun instant(): Instant = Instant.now()
    }
}
