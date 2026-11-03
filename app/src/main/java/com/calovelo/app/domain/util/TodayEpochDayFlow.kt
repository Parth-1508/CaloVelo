package com.calovelo.app.domain.util

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime

private const val MAX_POLL_MS = 60_000L

/**
 * Emits the current local epoch day and re-emits when midnight passes. Polls at most once
 * a minute because coroutine delays do not advance during deep sleep.
 */
fun todayEpochDayFlow(zoneProvider: () -> ZoneId = { ZoneId.systemDefault() }): Flow<Long> = flow {
    while (true) {
        val zone = zoneProvider()
        val now = ZonedDateTime.now(zone)
        emit(now.toLocalDate().toEpochDay())
        val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(zone)
        val untilMidnight = Duration.between(now, nextMidnight).toMillis() + 1_000L
        delay(minOf(untilMidnight, MAX_POLL_MS))
    }
}.distinctUntilChanged()
