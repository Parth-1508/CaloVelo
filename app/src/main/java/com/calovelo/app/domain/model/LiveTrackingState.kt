package com.calovelo.app.domain.model

/**
 * Snapshot of the in-progress recording, published by the repository and consumed by the
 * foreground service notification and the UI.
 *
 * [elapsedTimeSeconds] is moving time (paused time is excluded).
 * [currentPaceSecondsPerKm] is 0 when no pace can be computed (stationary / no GPS lock).
 */
data class LiveTrackingState(
    val isTracking: Boolean = false,
    val isPaused: Boolean = false,
    val elapsedTimeSeconds: Long = 0L,
    val distanceMeters: Double = 0.0,
    val currentPaceSecondsPerKm: Int = 0,
    val currentSpeedKmh: Double = 0.0,
    val estimatedCalories: Int = 0,
    val currentRoutePoints: List<LatLng> = emptyList(),
    val activityType: ActivityType = ActivityType.RUNNING
)
