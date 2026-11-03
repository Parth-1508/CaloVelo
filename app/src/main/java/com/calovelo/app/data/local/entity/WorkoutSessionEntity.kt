package com.calovelo.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "workout_session",
    indices = [Index(value = ["start_time"])]
)
data class WorkoutSessionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String = UUID.randomUUID().toString(),
    /** Origin of the record, e.g. "calovelo_gps" or (later) "health_connect". */
    @ColumnInfo(name = "source") val source: String,
    @ColumnInfo(name = "activity_type") val activityType: String,
    /** Epoch milliseconds (UTC). */
    @ColumnInfo(name = "start_time") val startTime: Long,
    @ColumnInfo(name = "end_time") val endTime: Long,
    /** Moving time; paused time is excluded. */
    @ColumnInfo(name = "duration_seconds") val durationSeconds: Long,
    @ColumnInfo(name = "distance_meters") val distanceMeters: Double,
    @ColumnInfo(name = "calories_burned") val caloriesBurned: Int,
    @ColumnInfo(name = "avg_heart_rate") val avgHeartRate: Int? = null,
    /** Google-encoded polyline (precision 5) of the recorded route. */
    @ColumnInfo(name = "route_polyline") val routePolyline: String? = null,
    @ColumnInfo(name = "rpe_score") val rpeScore: Int? = null,
    @ColumnInfo(name = "feeling_tag") val feelingTag: String? = null,
    @ColumnInfo(name = "user_notes") val userNotes: String? = null,
    @ColumnInfo(name = "is_shared_to_club") val isSharedToClub: Boolean = false
)
