package com.calovelo.app.domain.model

object WorkoutSource {
    const val APP_GPS = "calovelo_gps"
}

enum class FeelingTag(val label: String) {
    ENERGIZED("Energized"),
    ACCOMPLISHED("Accomplished"),
    FATIGUED("Fatigued"),
    MUSCLE_SORENESS("Muscle Soreness"),
    EXHAUSTED("Exhausted");

    companion object {
        fun fromName(name: String?): FeelingTag? = entries.firstOrNull { it.name == name }
    }
}

/** Rating of Perceived Exertion, 1 (very light) to 10 (maximum effort). */
object RpeScale {
    const val MIN = 1
    const val MAX = 10
    const val DEFAULT = 5

    private val descriptions = listOf(
        "Very Light",
        "Light",
        "Easy",
        "Moderate",
        "Somewhat Challenging",
        "Challenging",
        "Hard",
        "Very Hard",
        "Near Maximum",
        "Maximum Effort"
    )

    fun label(score: Int): String {
        val clamped = score.coerceIn(MIN, MAX)
        return "$clamped - ${descriptions[clamped - 1]}"
    }
}

data class WorkoutFeedback(
    val rpeScore: Int,
    val feelingTag: FeelingTag?,
    val notes: String
)

/** A saved workout that is waiting for the user's post-workout RPE feedback. */
data class PendingFeedback(
    val sessionId: String,
    val activityType: ActivityType,
    val durationSeconds: Long,
    val distanceMeters: Double
)

sealed interface FinishOutcome {
    data class Saved(val sessionId: String) : FinishOutcome
    data object TooShort : FinishOutcome
    data object NotRecording : FinishOutcome
}

enum class StartResult {
    STARTED,
    ALREADY_RECORDING,
    PERMISSION_MISSING,
    LOCATION_SERVICES_OFF,
    FAILED
}

/** Immutable result produced by the recorder when a workout is finished. */
data class RecordedActivity(
    val activityType: ActivityType,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val durationSeconds: Long,
    val distanceMeters: Double,
    val caloriesBurned: Int,
    val route: List<LatLng>
)

data class WorkoutSession(
    val id: String,
    val source: String,
    val activityType: String,
    val startTime: Long,
    val endTime: Long,
    val durationSeconds: Long,
    val distanceMeters: Double,
    val caloriesBurned: Int,
    val avgHeartRate: Int?,
    val route: List<LatLng>,
    val rpeScore: Int?,
    val feelingTag: FeelingTag?,
    val userNotes: String?,
    val isSharedToClub: Boolean
)
