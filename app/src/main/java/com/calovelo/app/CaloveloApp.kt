package com.calovelo.app

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.calovelo.app.core.notification.NotificationChannels
import com.calovelo.app.domain.repository.ReminderScheduler
import com.calovelo.app.domain.repository.SettingsRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class CaloveloApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var reminderScheduler: ReminderScheduler

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()
        NotificationChannels.register(this)

        // Re-assert the periodic schedule on launch (UPDATE policy keeps existing timing).
        // WorkManager itself persists the schedule across reboots.
        appScope.launch {
            if (settingsRepository.hydrationRemindersEnabled.first()) {
                reminderScheduler.scheduleHydrationReminders()
            }
        }
    }
}
