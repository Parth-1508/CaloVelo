// app/src/main/java/com/calovelo/app/data/local/entity/ExerciseSetEntity.kt
package com.calovelo.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * One logged set. Deleting the parent [GymWorkoutEntity] deletes its sets. [exerciseId] is
 * deliberately not a foreign key: history must survive any future change to the exercise catalog.
 */
@Entity(
    tableName = "exercise_set",
    foreignKeys = [
        ForeignKey(
            entity = GymWorkoutEntity::class,
            parentColumns = ["session_id"],
            childColumns = ["workout_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["workout_id", "exercise_id", "set_index"]),
        Index(value = ["exercise_id"])
    ]
)
data class ExerciseSetEntity(
    @PrimaryKey
    @ColumnInfo(name = "set_id") val setId: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "workout_id") val workoutId: String,
    @ColumnInfo(name = "exercise_id") val exerciseId: String,
    /** 1-based position among this exercise's sets in the workout. */
    @ColumnInfo(name = "set_index") val setIndex: Int,
    @ColumnInfo(name = "weight_kg") val weightKg: Double,
    @ColumnInfo(name = "reps") val reps: Int,
    /** Rate of perceived exertion 1..10 (halves allowed); null when not recorded. */
    @ColumnInfo(name = "rpe") val rpe: Double?,
    /** SetType name: NORMAL, WARMUP, DROPSET or FAILURE. */
    @ColumnInfo(name = "set_type") val setType: String
) {
    companion object {
        // Must equal SetType.name (SetType.fromName reads it back).
        const val TYPE_NORMAL = "NORMAL"
        const val TYPE_WARMUP = "WARMUP"
        const val TYPE_DROPSET = "DROPSET"
        const val TYPE_FAILURE = "FAILURE"
    }
}
