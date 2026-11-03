package com.calovelo.app.worker

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.calovelo.app.MainActivity
import com.calovelo.app.R
import com.calovelo.app.core.notification.NotificationChannels
import com.calovelo.app.domain.model.DailyHealthSummary
import com.calovelo.app.domain.model.HydrationSchedule
import com.calovelo.app.domain.repository.HealthRepository
import com.calovelo.app.domain.repository.SettingsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import java.time.LocalTime

/**
 * Hourly check: between 08:00 and 22:00, if today's intake trails the even-pace target by
 * at least one glass, post a reminder with a one-tap "+250 ml" action.
 */
@HiltWorker
class HydrationReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val healthRepository: HealthRepository,
    private val settingsRepository: SettingsRepository
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = try {
        if (!settingsRepository.hydrationRemindersEnabled.first()) {
            Result.success()
        } else {
            val now = LocalTime.now()
            if (HydrationSchedule.isWithinWindow(now)) {
                val summary = healthRepository.getToday()
                if (HydrationSchedule.isBehind(summary.waterIntakeMl, summary.waterGoalMl, now) &&
                    NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()
                ) {
                    postReminder(summary)
                }
            }
            Result.success()
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        if (runAttemptCount < MAX_RETRIES) Result.retry() else Result.success()
    }

    // Guarded by areNotificationsEnabled(), which accounts for POST_NOTIFICATIONS on API 33+.
    @SuppressLint("MissingPermission")
    private fun postReminder(summary: DailyHealthSummary) {
        val context = applicationContext

        val openApp = PendingIntent.getActivity(
            context,
            REQUEST_OPEN_APP,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val quickAdd = PendingIntent.getBroadcast(
            context,
            REQUEST_QUICK_ADD,
            Intent(context, WaterQuickAddReceiver::class.java).apply {
                action = WaterQuickAddReceiver.ACTION_QUICK_ADD
                putExtra(WaterQuickAddReceiver.EXTRA_AMOUNT_ML, QUICK_ADD_ML)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val text = "You're at ${summary.waterIntakeMl} of ${summary.waterGoalMl} ml today. " +
            "A glass now keeps you on pace."

        val notification = NotificationCompat.Builder(context, NotificationChannels.HYDRATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_water_drop)
            .setContentTitle("Time to hydrate")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOnlyAlertOnce(true)
            .setAutoCancel(true)
            .setContentIntent(openApp)
            .addAction(R.drawable.ic_water_drop, "+$QUICK_ADD_ML ml", quickAdd)
            .build()

        NotificationManagerCompat.from(context)
            .notify(NotificationChannels.HYDRATION_NOTIFICATION_ID, notification)
    }

    companion object {
        const val UNIQUE_WORK_NAME = "hydration_reminder_periodic"
        private const val QUICK_ADD_ML = 250
        private const val MAX_RETRIES = 3
        private const val REQUEST_OPEN_APP = 10
        private const val REQUEST_QUICK_ADD = 11
    }
}
