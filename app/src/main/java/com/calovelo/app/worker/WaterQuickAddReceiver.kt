package com.calovelo.app.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.calovelo.app.core.notification.NotificationChannels
import com.calovelo.app.domain.repository.HydrationRepository
import com.calovelo.app.domain.usecase.LogWaterUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Handles the notification's "+250 ml" action without opening the app. */
@AndroidEntryPoint
class WaterQuickAddReceiver : BroadcastReceiver() {

    @Inject lateinit var logWater: LogWaterUseCase

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_QUICK_ADD) return

        val amountMl = intent.getIntExtra(EXTRA_AMOUNT_ML, 0)
        if (amountMl !in HydrationRepository.MIN_LOG_ML..HydrationRepository.MAX_LOG_ML) return

        val pendingResult = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                logWater(amountMl)
                NotificationManagerCompat.from(appContext).cancel(NotificationChannels.HYDRATION_NOTIFICATION_ID)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to log quick-add water", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "WaterQuickAddReceiver"
        const val ACTION_QUICK_ADD = "com.calovelo.app.action.QUICK_ADD_WATER"
        const val EXTRA_AMOUNT_ML = "extra_amount_ml"
    }
}
