// app/src/main/java/com/calovelo/app/domain/model/GymModels.kt
package com.calovelo.app.domain.model

/** Primary muscle group of an exercise. [key] is the value stored in the DB and gym_seed.json. */
enum class MuscleGroup(val key: String, val displayName: String) {
    ABDOMINALS("ABDOMINALS", "Abs"),
    ABDUCTORS("ABDUCTORS", "Abductors"),
    ADDUCTORS("ADDUCTORS", "Adductors"),
    BICEPS("BICEPS", "Biceps"),
    CALVES("CALVES", "Calves"),
    CHEST("CHEST", "Chest"),
    FOREARMS("FOREARMS", "Forearms"),
    GLUTES("GLUTES", "Glutes"),
    HAMSTRINGS("HAMSTRINGS", "Hamstrings"),
    LATS("LATS", "Lats"),
    LOWER_BACK("LOWER_BACK", "Lower back"),
    MIDDLE_BACK("MIDDLE_BACK", "Middle back"),
    NECK("NECK", "Neck"),
    QUADRICEPS("QUADRICEPS", "Quads"),
    SHOULDERS("SHOULDERS", "Shoulders"),
    TRAPS("TRAPS", "Traps"),
    TRICEPS("TRICEPS", "Triceps"),
    OTHER("OTHER", "Other");

    companion object {
        /** Null when [key] is not a known group; used by the strict seed parser. */
        fun fromKeyOrNull(key: String?): MuscleGroup? =
            entries.firstOrNull { it.key.equals(key?.trim(), ignoreCase = true) }

        fun fromKey(key: String?): MuscleGroup = fromKeyOrNull(key) ?: OTHER

        /** Groups worth offering as filter chips (everything except the [OTHER] fallback). */
        val filterable: List<MuscleGroup> get() = entries.filter { it != OTHER }
    }
}

enum class EquipmentType(val key: String, val displayName: String) {
    BANDS("BANDS", "Bands"),
    BARBELL("BARBELL", "Barbell"),
    BODY_ONLY("BODY_ONLY", "Bodyweight"),
    CABLE("CABLE", "Cable"),
    DUMBBELL("DUMBBELL", "Dumbbell"),
    EZ_CURL_BAR("E-Z_CURL_BAR", "EZ curl bar"),
    EXERCISE_BALL("EXERCISE_BALL", "Exercise ball"),
    FOAM_ROLL("FOAM_ROLL", "Foam roll"),
    KETTLEBELLS("KETTLEBELLS", "Kettlebell"),
    MACHINE("MACHINE", "Machine"),
    MEDICINE_BALL("MEDICINE_BALL", "Medicine ball"),
    NONE("NONE", "None"),
    OTHER("OTHER", "Other");

    companion object {
        fun fromKeyOrNull(key: String?): EquipmentType? =
            entries.firstOrNull { it.key.equals(key?.trim(), ignoreCase = true) }

        fun fromKey(key: String?): EquipmentType = fromKeyOrNull(key) ?: OTHER
    }
}

enum class SetType {
    NORMAL, WARMUP, DROPSET, FAILURE;

    companion object {
        fun fromName(name: String?): SetType = entries.firstOrNull { it.name == name } ?: NORMAL
    }
}

/** One entry of the exercise catalog. */
data class Exercise(
    val id: String,
    val name: String,
    val muscleGroup: MuscleGroup,
    val equipmentType: EquipmentType,
    val instructions: String
)

/** A gym workout. It is active while [endTimeMillis] is null. */
data class GymSession(
    val sessionId: String,
    val epochDay: Long,
    val startTimeMillis: Long,
    val endTimeMillis: Long?,
    val notes: String?
) {
    val isActive: Boolean get() = endTimeMillis == null
}

/** One logged set. [setIndex] is 1-based within the (session, exercise) pair. */
data class GymSet(
    val setId: String,
    val sessionId: String,
    val exerciseId: String,
    val setIndex: Int,
    val weightKg: Double,
    val reps: Int,
    val rpe: Double?,
    val type: SetType
)

/** The in-progress session together with every set logged so far, in logging order. */
data class ActiveGymSession(
    val session: GymSession,
    val sets: List<GymSet>
)

/** A finished session with its aggregates, for the history list. */
data class GymSessionSummary(
    val session: GymSession,
    val setCount: Int,
    val totalVolumeKg: Double
) {
    val durationMillis: Long
        get() = ((session.endTimeMillis ?: session.startTimeMillis) - session.startTimeMillis).coerceAtLeast(0L)
}

data class DailyGymVolume(
    val epochDay: Long,
    val volumeKg: Double
)

sealed interface LogSetResult {
    data class Logged(val set: GymSet) : LogSetResult
    data object NoActiveSession : LogSetResult
    data object UnknownExercise : LogSetResult
    data class Invalid(val reason: String) : LogSetResult
}

sealed interface EditSetResult {
    data object Updated : EditSetResult
    /** The set no longer exists, or its session is already finished. */
    data object NotFound : EditSetResult
    data class Invalid(val reason: String) : EditSetResult
}

sealed interface FinishGymResult {
    data class Finished(val summary: GymSessionSummary) : FinishGymResult
    /** The session had no sets, so nothing was saved to history. */
    data object DiscardedEmpty : FinishGymResult
    data object NoActiveSession : FinishGymResult
}

/** Input limits shared by the use cases (validation) and the UI (steppers). */
object GymLimits {
    const val MAX_WEIGHT_KG = 1_000.0
    const val MAX_REPS = 200
    const val MIN_RPE = 1.0
    const val MAX_RPE = 10.0
    const val MAX_SEARCH_LIMIT = 100
    const val DEFAULT_SEARCH_LIMIT = 50
    const val MAX_NOTES_LENGTH = 1_000

    /** Returns a user-facing reason when the input is unacceptable, or null when it is fine. */
    fun validateSet(weightKg: Double, reps: Int, rpe: Double?): String? = when {
        !weightKg.isFinite() || weightKg < 0.0 || weightKg > MAX_WEIGHT_KG ->
            "Weight must be between 0 and ${MAX_WEIGHT_KG.toInt()} kg."
        reps !in 1..MAX_REPS -> "Reps must be between 1 and $MAX_REPS."
        rpe != null && (!rpe.isFinite() || rpe < MIN_RPE || rpe > MAX_RPE) ->
            "RPE must be between ${MIN_RPE.toInt()} and ${MAX_RPE.toInt()}."
        else -> null
    }
}
