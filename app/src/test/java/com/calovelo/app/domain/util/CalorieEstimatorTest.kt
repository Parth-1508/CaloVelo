package com.calovelo.app.domain.util

import com.calovelo.app.domain.model.ActivityType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalorieEstimatorTest {

    @Test
    fun met_atReferenceSpeeds_matchesTable() {
        assertEquals(8.3, CalorieEstimator.met(ActivityType.RUNNING, 8.0), 1e-9)
        assertEquals(3.5, CalorieEstimator.met(ActivityType.WALKING, 4.8), 1e-9)
        assertEquals(8.0, CalorieEstimator.met(ActivityType.OUTDOOR_CYCLING, 20.9), 1e-9)
    }

    @Test
    fun met_interpolatesBetweenPointsAndClampsAtEnds() {
        val midpoint = CalorieEstimator.met(ActivityType.WALKING, 4.4) // between 4.0->3.0 and 4.8->3.5
        assertEquals(3.25, midpoint, 1e-9)
        assertEquals(19.0, CalorieEstimator.met(ActivityType.RUNNING, 40.0), 1e-9)
        assertEquals(1.0, CalorieEstimator.met(ActivityType.RUNNING, -3.0), 1e-9)
    }

    @Test
    fun met_increasesWithSpeed() {
        ActivityType.entries.forEach { type ->
            var previous = 0.0
            for (speed in 0..35) {
                val met = CalorieEstimator.met(type, speed.toDouble())
                assertTrue("$type MET should not decrease at $speed km/h", met >= previous)
                previous = met
            }
        }
    }

    @Test
    fun activeKilocalories_isZeroWhenStationary_andScalesLinearly() {
        assertEquals(0.0, CalorieEstimator.activeKilocalories(ActivityType.RUNNING, 0.0, 70.0, 3600.0), 1e-9)

        val oneHour = CalorieEstimator.activeKilocalories(ActivityType.RUNNING, 8.0, 70.0, 3600.0)
        assertEquals((8.3 - 1.0) * 70.0, oneHour, 1e-6)

        val halfHour = CalorieEstimator.activeKilocalories(ActivityType.RUNNING, 8.0, 70.0, 1800.0)
        assertEquals(oneHour / 2, halfHour, 1e-6)
    }
}
