package com.calovelo.app.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.calovelo.app.domain.repository.ReminderScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkManagerReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) : ReminderScheduler {

    override fun scheduleHydrationReminders() {
        val request = PeriodicWorkRequestBuilder<HydrationReminderWorker>(1, TimeUnit.HOURS)
            .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            HydrationReminderWorker.UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    override fun cancelHydrationReminders() {
        WorkManager.getInstance(context).cancelUniqueWork(HydrationReminderWorker.UNIQUE_WORK_NAME)
    }
}
