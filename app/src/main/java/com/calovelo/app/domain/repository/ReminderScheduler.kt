package com.calovelo.app.domain.repository

interface ReminderScheduler {
    fun scheduleHydrationReminders()
    fun cancelHydrationReminders()
}
