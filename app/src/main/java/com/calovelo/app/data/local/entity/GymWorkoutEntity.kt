// app/src/main/java/com/calovelo/app/data/local/entity/GymWorkoutEntity.kt
package com.calovelo.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/** A gym session. It is the active session while [endTime] is null. */
@Entity(
    tableName = "gym_workout",
    indices = [Index(value = ["epoch_day"]), Index(value = ["start_time"])]
)
data class GymWorkoutEntity(
    @PrimaryKey
    @ColumnInfo(name = "session_id") val sessionId: String = UUID.randomUUID().toString(),
    /** Local calendar day (LocalDate.toEpochDay) the session started on. */
    @ColumnInfo(name = "epoch_day") val epochDay: Long,
    /** Epoch millis. */
    @ColumnInfo(name = "start_time") val startTime: Long,
    /** Epoch millis; null while the session is still in progress. */
    @ColumnInfo(name = "end_time") val endTime: Long?,
    @ColumnInfo(name = "notes") val notes: String?
)
