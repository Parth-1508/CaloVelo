package com.calovelo.app.domain.tracking

import com.calovelo.app.domain.model.ActivityType
import com.calovelo.app.domain.model.LatLng
import com.calovelo.app.domain.model.LiveTrackingState
import com.calovelo.app.domain.model.LocationFix
import com.calovelo.app.domain.model.RecordedActivity
import com.calovelo.app.domain.util.CalorieEstimator
import com.calovelo.app.domain.util.GeoMath
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Pure, framework-free state machine that turns raw GPS fixes and clock ticks into distance,
 * pace, calories and a route. All time inputs are explicit so the class is deterministic and
 * unit-testable; all public methods are synchronized because the service's timer and the
 * location callback may run on different threads in future.
 *
 * Anti-drift rules applied to every fix:
 *  1. Fixes with accuracy worse than [MAX_ACCURACY_METERS] (or no accuracy) are discarded.
 *  2. Fixes older than the start/resume gate (cached last-known locations) are discarded.
 *  3. Out-of-order fixes are discarded.
 *  4. Movement smaller than max([MIN_SEGMENT_METERS], accuracy / 2) is treated as GPS jitter.
 *  5. Segments implying a speed above the activity's plausible maximum are discarded.
 */
class ActivityRecorder(
    private val bodyWeightKg: Double = CalorieEstimator.DEFAULT_BODY_WEIGHT_KG
) {

    private enum class Phase { IDLE, RECORDING, PAUSED }

    private data class SpeedSample(val monotonicMillis: Long, val distanceMeters: Double)

    private var phase = Phase.IDLE
    private var activityType = ActivityType.RUNNING
    private var startWallClockMillis = 0L
    private var activeMillis = 0L
    private var distanceMeters = 0.0
    private var activeCaloriesKcal = 0.0
    private var speedMps = 0.0
    private var lastTickMonotonic = 0L
    private var lastAcceptedMonotonic = 0L
    private var acceptFixesFromMonotonic = 0L
    private var anchor: LocationFix? = null
    private val route = ArrayList<LatLng>()
    private var routeSnapshot: List<LatLng> = emptyList()
    private val speedWindow = ArrayDeque<SpeedSample>()

    @Synchronized
    fun start(type: ActivityType, wallClockMillis: Long, monotonicMillis: Long): Boolean {
        if (phase != Phase.IDLE) return false
        resetInternal()
        activityType = type
        startWallClockMillis = wallClockMillis
        lastTickMonotonic = monotonicMillis
        lastAcceptedMonotonic = monotonicMillis
        acceptFixesFromMonotonic = monotonicMillis
        phase = Phase.RECORDING
        return true
    }

    @Synchronized
    fun pause(monotonicMillis: Long): Boolean {
        if (phase != Phase.RECORDING) return false
        advance(monotonicMillis)
        phase = Phase.PAUSED
        speedMps = 0.0
        speedWindow.clear()
        return true
    }

    @Synchronized
    fun resume(monotonicMillis: Long): Boolean {
        if (phase != Phase.PAUSED) return false
        lastTickMonotonic = monotonicMillis
        lastAcceptedMonotonic = monotonicMillis
        acceptFixesFromMonotonic = monotonicMillis
        // The user may have moved while paused; the next fix becomes a fresh anchor so the
        // paused gap is never counted as distance.
        anchor = null
        speedWindow.clear()
        speedMps = 0.0
        phase = Phase.RECORDING
        return true
    }

    @Synchronized
    fun onTick(monotonicMillis: Long) {
        advance(monotonicMillis)
    }

    /** @return true when the fix was accepted and the recorded state changed. */
    @Synchronized
    fun onLocation(fix: LocationFix): Boolean {
        if (phase != Phase.RECORDING) return false

        val accuracy = fix.accuracyMeters ?: return false
        if (accuracy > MAX_ACCURACY_METERS) return false
        if (fix.latitude !in -90.0..90.0 || fix.longitude !in -180.0..180.0) return false
        if (fix.elapsedRealtimeMillis < acceptFixesFromMonotonic) return false

        val previous = anchor
        if (previous == null) {
            anchor = fix
            addRoutePoint(fix)
            lastAcceptedMonotonic = fix.elapsedRealtimeMillis
            speedWindow.clear()
            speedWindow.addLast(SpeedSample(fix.elapsedRealtimeMillis, distanceMeters))
            return true
        }

        val deltaMillis = fix.elapsedRealtimeMillis - previous.elapsedRealtimeMillis
        if (deltaMillis <= 0L) return false

        val segmentMeters = GeoMath.haversineMeters(
            previous.latitude, previous.longitude, fix.latitude, fix.longitude
        )
        if (segmentMeters < max(MIN_SEGMENT_METERS, accuracy * 0.5)) return false

        val impliedSpeed = segmentMeters / (deltaMillis / 1000.0)
        if (impliedSpeed > activityType.maxPlausibleSpeedMps) return false

        distanceMeters += segmentMeters
        anchor = fix
        addRoutePoint(fix)
        lastAcceptedMonotonic = fix.elapsedRealtimeMillis
        updateSpeed(fix.elapsedRealtimeMillis)
        return true
    }

    @Synchronized
    fun snapshot(): LiveTrackingState = LiveTrackingState(
        isTracking = phase != Phase.IDLE,
        isPaused = phase == Phase.PAUSED,
        elapsedTimeSeconds = activeMillis / 1000L,
        distanceMeters = distanceMeters,
        currentPaceSecondsPerKm = paceSecondsPerKm(),
        currentSpeedKmh = speedMps * 3.6,
        estimatedCalories = activeCaloriesKcal.roundToInt(),
        currentRoutePoints = routeSnapshot,
        activityType = activityType
    )

    /** Builds the final result without changing state; call [reset] once it has been persisted. */
    @Synchronized
    fun buildResult(wallClockMillis: Long, monotonicMillis: Long): RecordedActivity? {
        if (phase == Phase.IDLE) return null
        advance(monotonicMillis)
        return RecordedActivity(
            activityType = activityType,
            startTimeMillis = startWallClockMillis,
            endTimeMillis = max(wallClockMillis, startWallClockMillis),
            durationSeconds = (activeMillis + 500L) / 1000L,
            distanceMeters = distanceMeters,
            caloriesBurned = activeCaloriesKcal.roundToInt(),
            route = routeSnapshot
        )
    }

    @Synchronized
    fun reset() {
        resetInternal()
    }

    private fun advance(now: Long) {
        val delta = (now - lastTickMonotonic).coerceAtLeast(0L)
        lastTickMonotonic = now
        if (phase != Phase.RECORDING || delta == 0L) return

        activeMillis += delta
        // No accepted fix for a while means the user has stopped (or lost GPS): decay speed to 0.
        if (now - lastAcceptedMonotonic > STALE_FIX_MILLIS) speedMps = 0.0
        activeCaloriesKcal += CalorieEstimator.activeKilocalories(
            activityType = activityType,
            speedKmh = speedMps * 3.6,
            weightKg = bodyWeightKg,
            durationSeconds = delta / 1000.0
        )
    }

    private fun updateSpeed(now: Long) {
        speedWindow.addLast(SpeedSample(now, distanceMeters))
        while (speedWindow.size >= 2 && now - speedWindow[1].monotonicMillis >= SPEED_WINDOW_MILLIS) {
            speedWindow.removeFirst()
        }
        val oldest = speedWindow.first()
        val spanMillis = now - oldest.monotonicMillis
        if (spanMillis >= MIN_SPEED_SPAN_MILLIS) {
            speedMps = (distanceMeters - oldest.distanceMeters) / (spanMillis / 1000.0)
        }
    }

    private fun addRoutePoint(fix: LocationFix) {
        route.add(LatLng(fix.latitude, fix.longitude))
        routeSnapshot = route.toList()
    }

    private fun paceSecondsPerKm(): Int =
        if (speedMps >= MIN_PACE_SPEED_MPS) {
            (1000.0 / speedMps).roundToInt().coerceAtMost(MAX_PACE_SECONDS)
        } else {
            0
        }

    private fun resetInternal() {
        phase = Phase.IDLE
        activityType = ActivityType.RUNNING
        startWallClockMillis = 0L
        activeMillis = 0L
        distanceMeters = 0.0
        activeCaloriesKcal = 0.0
        speedMps = 0.0
        lastTickMonotonic = 0L
        lastAcceptedMonotonic = 0L
        acceptFixesFromMonotonic = 0L
        anchor = null
        route.clear()
        routeSnapshot = emptyList()
        speedWindow.clear()
    }

    companion object {
        /** GPS fixes with a worse (larger) horizontal accuracy radius than this are discarded. */
        const val MAX_ACCURACY_METERS = 25f
        const val MIN_SEGMENT_METERS = 2.0
        private const val STALE_FIX_MILLIS = 10_000L
        private const val SPEED_WINDOW_MILLIS = 10_000L
        private const val MIN_SPEED_SPAN_MILLIS = 2_000L
        private const val MIN_PACE_SPEED_MPS = 0.3
        private const val MAX_PACE_SECONDS = 59 * 60 + 59
    }
}
