package com.calovelo.app.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.calovelo.app.domain.model.StartResult
import com.calovelo.app.domain.repository.WorkoutServiceLauncher
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutServiceLauncherImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : WorkoutServiceLauncher {

    override fun start(): StartResult {
        // Android 14+: a location-type foreground service may only start while the app holds
        // a location permission, otherwise startForeground() throws SecurityException.
        val hasFineLocation = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasFineLocation) return StartResult.PERMISSION_MISSING

        val locationManager = context.getSystemService(LocationManager::class.java)
        if (locationManager != null && !LocationManagerCompat.isLocationEnabled(locationManager)) {
            return StartResult.LOCATION_SERVICES_OFF
        }

        return try {
            ContextCompat.startForegroundService(context, WorkoutTrackingService.startIntent(context))
            StartResult.STARTED
        } catch (e: SecurityException) {
            Log.e(TAG, "Foreground service start rejected", e)
            StartResult.FAILED
        } catch (e: IllegalStateException) {
            // e.g. ForegroundServiceStartNotAllowedException when the app is in the background.
            Log.e(TAG, "Foreground service start not allowed", e)
            StartResult.FAILED
        }
    }

    private companion object {
        const val TAG = "WorkoutServiceLauncher"
    }
}
