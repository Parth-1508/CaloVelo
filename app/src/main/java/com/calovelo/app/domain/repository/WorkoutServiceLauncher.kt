package com.calovelo.app.domain.repository

import com.calovelo.app.domain.model.StartResult

/** Starts the platform foreground service that feeds GPS data into [WorkoutRepository]. */
interface WorkoutServiceLauncher {
    /** @return [StartResult.STARTED] or the reason the service could not be started. */
    fun start(): StartResult
}
