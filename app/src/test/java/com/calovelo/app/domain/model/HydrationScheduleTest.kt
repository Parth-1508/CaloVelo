package com.calovelo.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class HydrationScheduleTest {

    @Test
    fun window_isStartInclusiveEndExclusive() {
        assertFalse(HydrationSchedule.isWithinWindow(LocalTime.of(7, 59)))
        assertTrue(HydrationSchedule.isWithinWindow(LocalTime.of(8, 0)))
        assertTrue(HydrationSchedule.isWithinWindow(LocalTime.of(21, 59)))
        assertFalse(HydrationSchedule.isWithinWindow(LocalTime.of(22, 0)))
    }

    @Test
    fun expectedIntake_isLinearAcrossWindow() {
        assertEquals(0, HydrationSchedule.expectedIntakeMl(2800, LocalTime.of(8, 0)))
        assertEquals(1400, HydrationSchedule.expectedIntakeMl(2800, LocalTime.of(15, 0)))
        assertEquals(2800, HydrationSchedule.expectedIntakeMl(2800, LocalTime.of(22, 0)))
    }

    @Test
    fun isBehind_requiresAtLeastOneGlassOfDeficit() {
        val noon = LocalTime.of(12, 0) // expected 800 of 2800
        assertTrue(HydrationSchedule.isBehind(intakeMl = 500, goalMl = 2800, now = noon))
        assertFalse(HydrationSchedule.isBehind(intakeMl = 600, goalMl = 2800, now = noon))
    }

    @Test
    fun isBehind_isFalseOutsideWindowOrWhenGoalReached() {
        assertFalse(HydrationSchedule.isBehind(0, 2500, LocalTime.of(6, 0)))
        assertFalse(HydrationSchedule.isBehind(0, 2500, LocalTime.of(23, 0)))
        assertFalse(HydrationSchedule.isBehind(2500, 2500, LocalTime.of(20, 0)))
    }
}
