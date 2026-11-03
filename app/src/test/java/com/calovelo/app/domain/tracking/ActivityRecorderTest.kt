package com.calovelo.app.domain.tracking

import com.calovelo.app.domain.model.ActivityType
import com.calovelo.app.domain.model.LocationFix
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ActivityRecorderTest {

    private val baseLat = 18.5200
    private val baseLng = 73.8500

    /** 0.0001 degrees of latitude is ~11.12 m. */
    private fun fixAt(step: Int, tMillis: Long, accuracy: Float? = 5f) =
        LocationFix(baseLat + step * 0.0001, baseLng, accuracy, tMillis)

    private fun startedRecorder(type: ActivityType = ActivityType.OUTDOOR_CYCLING): ActivityRecorder {
        val recorder = ActivityRecorder(bodyWeightKg = 70.0)
        assertTrue(recorder.start(type, wallClockMillis = 1_000_000L, monotonicMillis = 0L))
        return recorder
    }

    @Test
    fun accuracyWorseThan25Meters_isDiscarded_andExactly25IsAccepted() {
        val recorder = startedRecorder()
        assertFalse(recorder.onLocation(fixAt(0, 1_000, accuracy = 25.1f)))
        assertFalse(recorder.onLocation(fixAt(0, 1_000, accuracy = null)))
        assertTrue(recorder.snapshot().currentRoutePoints.isEmpty())

        assertTrue(recorder.onLocation(fixAt(0, 1_000, accuracy = 25f)))
        assertEquals(1, recorder.snapshot().currentRoutePoints.size)
    }

    @Test
    fun movingFixes_accumulateDistanceRouteAndSpeed() {
        val recorder = startedRecorder()
        for (i in 0..9) {
            recorder.onTick(i * 1_000L)
            recorder.onLocation(fixAt(i, 1_000L + i * 1_000L))
        }
        val state = recorder.snapshot()
        assertEquals(9 * 11.12, state.distanceMeters, 1.0)
        assertEquals(10, state.currentRoutePoints.size)
        assertEquals(11.12 * 3.6, state.currentSpeedKmh, 1.5)
        assertTrue(state.currentPaceSecondsPerKm in 85..95) // ~1000 / 11.12 s per km
        assertTrue(state.estimatedCalories > 0)
    }

    @Test
    fun stationaryJitterWithinThreshold_doesNotAddDistance() {
        val recorder = startedRecorder(ActivityType.RUNNING)
        assertTrue(recorder.onLocation(fixAt(0, 1_000)))
        // ~0.5 m of wobble, below the 2 m minimum segment.
        for (i in 1..20) {
            val wobble = LocationFix(baseLat + 0.000004 * (i % 2), baseLng, 8f, 1_000L + i * 1_000L)
            assertFalse(recorder.onLocation(wobble))
        }
        assertEquals(0.0, recorder.snapshot().distanceMeters, 1e-9)
    }

    @Test
    fun implausibleSpeedJump_isRejected() {
        val recorder = startedRecorder(ActivityType.WALKING)
        assertTrue(recorder.onLocation(fixAt(0, 1_000)))
        // ~111 m in one second = 111 m/s: a GPS glitch for a walker.
        assertFalse(recorder.onLocation(fixAt(10, 2_000)))
        assertEquals(0.0, recorder.snapshot().distanceMeters, 1e-9)
    }

    @Test
    fun fixesOlderThanStart_areIgnored() {
        val recorder = ActivityRecorder()
        recorder.start(ActivityType.RUNNING, wallClockMillis = 1L, monotonicMillis = 50_000L)
        assertFalse(recorder.onLocation(fixAt(0, 49_000L))) // cached last-known location
        assertTrue(recorder.onLocation(fixAt(0, 50_500L)))
    }

    @Test
    fun pause_stopsTimeAndDistance_andResumeDoesNotCountTheGap() {
        val recorder = startedRecorder()
        recorder.onLocation(fixAt(0, 1_000))
        recorder.onTick(5_000)
        assertEquals(5L, recorder.snapshot().elapsedTimeSeconds)

        assertTrue(recorder.pause(5_000))
        recorder.onTick(15_000)
        assertFalse(recorder.onLocation(fixAt(50, 15_000)))
        assertEquals(5L, recorder.snapshot().elapsedTimeSeconds)
        assertTrue(recorder.snapshot().isPaused)

        assertTrue(recorder.resume(20_000))
        // First fix after resume becomes a fresh anchor: ~550 m away but not counted.
        assertTrue(recorder.onLocation(fixAt(50, 20_500)))
        assertEquals(0.0, recorder.snapshot().distanceMeters, 1e-9)

        recorder.onTick(25_000)
        assertEquals(10L, recorder.snapshot().elapsedTimeSeconds)
    }

    @Test
    fun speedDecaysToZeroAfterGpsStopsReportingMovement() {
        val recorder = startedRecorder()
        for (i in 0..9) {
            recorder.onTick(i * 1_000L)
            recorder.onLocation(fixAt(i, 1_000L + i * 1_000L))
        }
        assertTrue(recorder.snapshot().currentSpeedKmh > 10.0)

        recorder.onTick(40_000)
        assertEquals(0.0, recorder.snapshot().currentSpeedKmh, 1e-9)
        assertEquals(0, recorder.snapshot().currentPaceSecondsPerKm)
    }

    @Test
    fun buildResult_isNonDestructive_andResetReturnsToIdle() {
        val recorder = startedRecorder()
        for (i in 0..4) recorder.onLocation(fixAt(i, 1_000L + i * 1_000L))
        recorder.onTick(30_000)

        val result = recorder.buildResult(wallClockMillis = 2_000_000L, monotonicMillis = 30_000)
        assertNotNull(result)
        assertEquals(30L, result!!.durationSeconds)
        assertEquals(1_000_000L, result.startTimeMillis)
        assertEquals(2_000_000L, result.endTimeMillis)
        assertEquals(5, result.route.size)
        assertTrue(recorder.snapshot().isTracking)

        recorder.reset()
        assertFalse(recorder.snapshot().isTracking)
        assertNull(recorder.buildResult(2_000_000L, 31_000))
    }

    @Test
    fun start_isRejectedWhileRecording() {
        val recorder = startedRecorder()
        assertFalse(recorder.start(ActivityType.RUNNING, 0L, 0L))
    }
}
