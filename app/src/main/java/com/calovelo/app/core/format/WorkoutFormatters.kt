package com.calovelo.app.core.format

import java.util.Locale

object WorkoutFormatters {

    /** HH:MM:SS */
    fun formatElapsed(totalSeconds: Long): String {
        val seconds = totalSeconds.coerceAtLeast(0L)
        return String.format(
            Locale.US,
            "%02d:%02d:%02d",
            seconds / 3600,
            (seconds % 3600) / 60,
            seconds % 60
        )
    }

    /** Kilometres with two decimals, using the device's decimal separator. */
    fun formatDistanceKm(meters: Double): String =
        String.format(Locale.getDefault(), "%.2f", meters.coerceAtLeast(0.0) / 1000.0)

    /** m:ss per km, or "--:--" when no pace is available. */
    fun formatPace(secondsPerKm: Int): String =
        if (secondsPerKm <= 0) {
            "--:--"
        } else {
            String.format(Locale.US, "%d:%02d", secondsPerKm / 60, secondsPerKm % 60)
        }

    fun formatSpeedKmh(speedKmh: Double): String =
        String.format(Locale.getDefault(), "%.1f", speedKmh.coerceAtLeast(0.0))
}
