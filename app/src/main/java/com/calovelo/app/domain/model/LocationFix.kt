package com.calovelo.app.domain.model

/**
 * A single GPS fix, decoupled from android.location.Location.
 *
 * @property accuracyMeters horizontal accuracy radius, or null if the provider did not report one.
 * @property elapsedRealtimeMillis monotonic timestamp (SystemClock.elapsedRealtime base).
 */
data class LocationFix(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float?,
    val elapsedRealtimeMillis: Long
)
