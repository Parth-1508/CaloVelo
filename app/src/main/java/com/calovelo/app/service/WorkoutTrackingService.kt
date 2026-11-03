package com.calovelo.app.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.calovelo.app.MainActivity
import com.calovelo.app.R
import com.calovelo.app.core.format.WorkoutFormatters
import com.calovelo.app.core.notification.NotificationChannels
import com.calovelo.app.domain.model.FinishOutcome
import com.calovelo.app.domain.model.LiveTrackingState
import com.calovelo.app.domain.model.LocationFix
import com.calovelo.app.domain.repository.WorkoutRepository
import com.calovelo.app.presentation.navigation.Routes
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Foreground service that keeps a workout recording alive while the app is in the background.
 *
 * Android 14/15 compliance:
 *  - Manifest declares android:foregroundServiceType="location" plus the FOREGROUND_SERVICE and
 *    FOREGROUND_SERVICE_LOCATION permissions.
 *  - startForeground() is called with FOREGROUND_SERVICE_TYPE_LOCATION, i.e. the same type, and
 *    is always the first thing done after ACTION_START so the 5-second deadline is met.
 *  - It is only started from the foreground UI after ACCESS_FINE_LOCATION was granted
 *    (see WorkoutServiceLauncherImpl).
 *
 * The service owns the 1 Hz timer and the FusedLocationProviderClient subscription; all state
 * lives in [WorkoutRepository], which this service and the UI both observe.
 */
@AndroidEntryPoint
class WorkoutTrackingService : Service() {

    @Inject lateinit var repository: WorkoutRepository
    @Inject lateinit var fusedLocationClient: FusedLocationProviderClient

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var tickerJob: Job? = null
    private var stateJob: Job? = null
    private var isRequestingLocation = false
    private var foregroundStarted = false
    private var finishInFlight = false

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            for (location in result.locations) {
                repository.recordLocation(location.toFix())
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> handleStart()
            ACTION_TOGGLE_PAUSE -> if (foregroundStarted) handleTogglePause() else stopSelf()
            ACTION_FINISH -> if (foregroundStarted) handleFinish() else stopSelf()
            else -> if (!foregroundStarted) stopSelf()
        }
        // A killed process loses the in-memory recording, so never resurrect an empty service.
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        if (isRequestingLocation) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            isRequestingLocation = false
        }
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun handleStart() {
        val state = repository.trackingState.value
        if (!enterForeground(state)) {
            repository.discardRecording()
            stopSelf()
            return
        }
        if (!state.isTracking) {
            // The repository rejected or discarded the recording before we got here.
            shutdown()
            return
        }
        ensureTicker()
        ensureStateObserver()
    }

    @SuppressLint("InlinedApi")
    private fun enterForeground(state: LiveTrackingState): Boolean = try {
        ServiceCompat.startForeground(
            this,
            NotificationChannels.WORKOUT_NOTIFICATION_ID,
            buildNotification(state),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
        )
        foregroundStarted = true
        true
    } catch (e: SecurityException) {
        Log.e(TAG, "startForeground rejected (missing permission or type)", e)
        false
    } catch (e: IllegalStateException) {
        Log.e(TAG, "startForeground not allowed", e)
        false
    }

    private fun handleTogglePause() {
        if (repository.trackingState.value.isPaused) {
            repository.resumeRecording()
        } else {
            repository.pauseRecording()
        }
    }

    private fun handleFinish() {
        if (finishInFlight) return
        finishInFlight = true
        serviceScope.launch {
            try {
                val outcome = repository.finishRecording()
                if (outcome is FinishOutcome.Saved) postCompletionNotification()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Save failed: the recording is intact, so stay alive and let the user retry.
                Log.e(TAG, "Failed to save workout", e)
                finishInFlight = false
                return@launch
            }
            shutdown()
        }
    }

    private fun ensureTicker() {
        if (tickerJob?.isActive == true) return
        tickerJob = serviceScope.launch {
            while (isActive) {
                repository.recordTick(SystemClock.elapsedRealtime())
                delay(TICK_INTERVAL_MS)
            }
        }
    }

    private fun ensureStateObserver() {
        if (stateJob?.isActive == true) return
        stateJob = serviceScope.launch {
            repository.trackingState.collect { state ->
                if (state.isTracking) {
                    refreshNotification(state)
                    // Stop GPS while paused to save battery; resume when the user resumes.
                    setLocationUpdatesActive(!state.isPaused)
                } else if (!finishInFlight) {
                    shutdown()
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun setLocationUpdatesActive(active: Boolean) {
        if (active == isRequestingLocation) return
        if (active) {
            if (!hasFineLocationPermission()) {
                Log.w(TAG, "Fine location permission missing; cannot request updates")
                return
            }
            val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, LOCATION_INTERVAL_MS)
                .setMinUpdateIntervalMillis(LOCATION_MIN_INTERVAL_MS)
                .setWaitForAccurateLocation(false)
                .build()
            try {
                fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
                isRequestingLocation = true
            } catch (e: SecurityException) {
                Log.e(TAG, "Location updates rejected", e)
            }
        } else {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            isRequestingLocation = false
        }
    }

    private fun shutdown() {
        tickerJob?.cancel()
        tickerJob = null
        stateJob?.cancel()
        stateJob = null
        setLocationUpdatesActive(false)
        if (foregroundStarted) {
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
            foregroundStarted = false
        }
        stopSelf()
    }

    private fun hasFineLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    // --- Notifications -------------------------------------------------------------------

    @SuppressLint("MissingPermission") // Guarded by areNotificationsEnabled().
    private fun refreshNotification(state: LiveTrackingState) {
        val manager = NotificationManagerCompat.from(this)
        if (manager.areNotificationsEnabled()) {
            manager.notify(NotificationChannels.WORKOUT_NOTIFICATION_ID, buildNotification(state))
        }
    }

    private fun buildNotification(state: LiveTrackingState): Notification {
        val elapsed = WorkoutFormatters.formatElapsed(state.elapsedTimeSeconds)
        val title = buildString {
            append(state.activityType.displayName)
            append(" · ")
            append(elapsed)
            if (state.isPaused) append(" (Paused)")
        }
        val text = "${WorkoutFormatters.formatDistanceKm(state.distanceMeters)} km · " +
            "${WorkoutFormatters.formatPace(state.currentPaceSecondsPerKm)} /km"

        val toggleAction = if (state.isPaused) {
            NotificationCompat.Action.Builder(
                R.drawable.ic_play_arrow, "Resume",
                servicePendingIntent(ACTION_TOGGLE_PAUSE, REQUEST_TOGGLE)
            ).build()
        } else {
            NotificationCompat.Action.Builder(
                R.drawable.ic_pause, "Pause",
                servicePendingIntent(ACTION_TOGGLE_PAUSE, REQUEST_TOGGLE)
            ).build()
        }
        val finishAction = NotificationCompat.Action.Builder(
            R.drawable.ic_stop, "Finish",
            servicePendingIntent(ACTION_FINISH, REQUEST_FINISH)
        ).build()

        return NotificationCompat.Builder(this, NotificationChannels.WORKOUT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_directions_run)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(openAppPendingIntent())
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .addAction(toggleAction)
            .addAction(finishAction)
            .build()
    }

    @SuppressLint("MissingPermission") // Guarded by areNotificationsEnabled().
    private fun postCompletionNotification() {
        val manager = NotificationManagerCompat.from(this)
        if (!manager.areNotificationsEnabled()) return
        val pending = repository.pendingFeedback.value ?: return

        val notification = NotificationCompat.Builder(this, NotificationChannels.WORKOUT_SUMMARY_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_directions_run)
            .setContentTitle("Workout saved")
            .setContentText(
                "${WorkoutFormatters.formatDistanceKm(pending.distanceMeters)} km in " +
                    "${WorkoutFormatters.formatElapsed(pending.durationSeconds)}. Tap to rate your effort."
            )
            .setContentIntent(openAppPendingIntent())
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .build()
        manager.notify(NotificationChannels.WORKOUT_SUMMARY_NOTIFICATION_ID, notification)
    }

    private fun servicePendingIntent(action: String, requestCode: Int): PendingIntent =
        PendingIntent.getService(
            this,
            requestCode,
            Intent(this, WorkoutTrackingService::class.java).setAction(action),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    private fun openAppPendingIntent(): PendingIntent =
        PendingIntent.getActivity(
            this,
            REQUEST_OPEN_APP,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(MainActivity.EXTRA_OPEN_ROUTE, Routes.TRACKING)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    private fun Location.toFix(): LocationFix = LocationFix(
        latitude = latitude,
        longitude = longitude,
        accuracyMeters = if (hasAccuracy()) accuracy else null,
        elapsedRealtimeMillis = elapsedRealtimeNanos / 1_000_000L
    )

    companion object {
        private const val TAG = "WorkoutTrackingService"
        const val ACTION_START = "com.calovelo.app.action.WORKOUT_START"
        const val ACTION_TOGGLE_PAUSE = "com.calovelo.app.action.WORKOUT_TOGGLE_PAUSE"
        const val ACTION_FINISH = "com.calovelo.app.action.WORKOUT_FINISH"

        private const val TICK_INTERVAL_MS = 1_000L
        private const val LOCATION_INTERVAL_MS = 1_000L
        private const val LOCATION_MIN_INTERVAL_MS = 1_000L

        private const val REQUEST_OPEN_APP = 20
        private const val REQUEST_TOGGLE = 21
        private const val REQUEST_FINISH = 22

        fun startIntent(context: Context): Intent =
            Intent(context, WorkoutTrackingService::class.java).setAction(ACTION_START)
    }
}
