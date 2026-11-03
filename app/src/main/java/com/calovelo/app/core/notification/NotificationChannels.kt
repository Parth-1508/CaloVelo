package com.calovelo.app.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

object NotificationChannels {
    const val HYDRATION_CHANNEL_ID = "hydration_reminders"
    const val HYDRATION_NOTIFICATION_ID = 1001

    /** Ongoing foreground-service notification while a workout is being recorded. */
    const val WORKOUT_CHANNEL_ID = "workout_tracking"
    const val WORKOUT_NOTIFICATION_ID = 2001

    /** "Workout saved, rate your effort" notification after finishing from the notification. */
    const val WORKOUT_SUMMARY_CHANNEL_ID = "workout_summary"
    const val WORKOUT_SUMMARY_NOTIFICATION_ID = 2002

    fun register(context: Context) {
        val hydration = NotificationChannel(
            HYDRATION_CHANNEL_ID,
            "Hydration reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Gentle nudges when you're behind on your daily water goal."
        }

        val workout = NotificationChannel(
            WORKOUT_CHANNEL_ID,
            "Workout tracking",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Live time, distance and pace while a workout is being recorded."
            setShowBadge(false)
        }

        val summary = NotificationChannel(
            WORKOUT_SUMMARY_CHANNEL_ID,
            "Workout summaries",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Confirms a saved workout and prompts you to rate your effort."
        }

        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannels(listOf(hydration, workout, summary))
    }
}
