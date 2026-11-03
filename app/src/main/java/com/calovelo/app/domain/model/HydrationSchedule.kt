package com.calovelo.app.domain.model

import java.time.Duration
import java.time.LocalTime
import kotlin.math.roundToInt

/**
 * Pure scheduling rules for hydration reminders. The daily goal is assumed to be spread
 * linearly across the 08:00-22:00 window; the user is "behind" when they trail that
 * pace by at least one glass (250 ml).
 */
object HydrationSchedule {
    val WINDOW_START: LocalTime = LocalTime.of(8, 0)
    val WINDOW_END: LocalTime = LocalTime.of(22, 0)
    const val BEHIND_THRESHOLD_ML = 250

    fun isWithinWindow(now: LocalTime): Boolean =
        !now.isBefore(WINDOW_START) && now.isBefore(WINDOW_END)

    fun expectedIntakeMl(goalMl: Int, now: LocalTime): Int {
        if (now.isBefore(WINDOW_START)) return 0
        if (!now.isBefore(WINDOW_END)) return goalMl
        val totalMinutes = Duration.between(WINDOW_START, WINDOW_END).toMinutes().toDouble()
        val elapsedMinutes = Duration.between(WINDOW_START, now).toMinutes().toDouble()
        return (goalMl * (elapsedMinutes / totalMinutes)).roundToInt()
    }

    fun isBehind(intakeMl: Int, goalMl: Int, now: LocalTime): Boolean =
        isWithinWindow(now) &&
            intakeMl < goalMl &&
            expectedIntakeMl(goalMl, now) - intakeMl >= BEHIND_THRESHOLD_ML
}
