package com.calovelo.app.domain.usecase

import com.calovelo.app.domain.model.ActivityType
import com.calovelo.app.domain.model.FinishOutcome
import com.calovelo.app.domain.model.StartResult
import com.calovelo.app.domain.model.WorkoutFeedback
import com.calovelo.app.domain.repository.WorkoutRepository
import com.calovelo.app.domain.repository.WorkoutServiceLauncher
import javax.inject.Inject

class StartWorkoutUseCase @Inject constructor(
    private val repository: WorkoutRepository,
    private val serviceLauncher: WorkoutServiceLauncher
) {
    operator fun invoke(activityType: ActivityType): StartResult {
        if (!repository.startRecording(activityType)) return StartResult.ALREADY_RECORDING
        val result = serviceLauncher.start()
        if (result != StartResult.STARTED) repository.discardRecording()
        return result
    }
}

class PauseWorkoutUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    operator fun invoke(): Boolean = repository.pauseRecording()
}

class ResumeWorkoutUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    operator fun invoke(): Boolean = repository.resumeRecording()
}

class FinishWorkoutUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(): FinishOutcome = repository.finishRecording()
}

class SaveWorkoutFeedbackUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(sessionId: String, feedback: WorkoutFeedback) =
        repository.saveFeedback(sessionId, feedback)
}

class DismissWorkoutFeedbackUseCase @Inject constructor(
    private val repository: WorkoutRepository
) {
    operator fun invoke() = repository.dismissFeedback()
}
