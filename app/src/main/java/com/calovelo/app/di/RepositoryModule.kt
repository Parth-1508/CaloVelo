package com.calovelo.app.di

import com.calovelo.app.data.repository.HealthRepositoryImpl
import com.calovelo.app.data.repository.HydrationRepositoryImpl
import com.calovelo.app.data.repository.WorkoutRepositoryImpl
import com.calovelo.app.data.settings.DataStoreSettingsRepository
import com.calovelo.app.domain.repository.HealthRepository
import com.calovelo.app.domain.repository.HydrationRepository
import com.calovelo.app.domain.repository.ReminderScheduler
import com.calovelo.app.domain.repository.SettingsRepository
import com.calovelo.app.domain.repository.WorkoutRepository
import com.calovelo.app.domain.repository.WorkoutServiceLauncher
import com.calovelo.app.service.WorkoutServiceLauncherImpl
import com.calovelo.app.worker.WorkManagerReminderScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindHealthRepository(impl: HealthRepositoryImpl): HealthRepository

    @Binds
    abstract fun bindHydrationRepository(impl: HydrationRepositoryImpl): HydrationRepository

    @Binds
    abstract fun bindWorkoutRepository(impl: WorkoutRepositoryImpl): WorkoutRepository

    @Binds
    abstract fun bindWorkoutServiceLauncher(impl: WorkoutServiceLauncherImpl): WorkoutServiceLauncher

    @Binds
    abstract fun bindSettingsRepository(impl: DataStoreSettingsRepository): SettingsRepository

    @Binds
    abstract fun bindReminderScheduler(impl: WorkManagerReminderScheduler): ReminderScheduler
}
