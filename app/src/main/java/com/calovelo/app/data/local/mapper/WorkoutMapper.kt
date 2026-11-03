package com.calovelo.app.data.local.mapper

import com.calovelo.app.data.local.entity.WorkoutSessionEntity
import com.calovelo.app.domain.model.FeelingTag
import com.calovelo.app.domain.model.LatLng
import com.calovelo.app.domain.model.RecordedActivity
import com.calovelo.app.domain.model.WorkoutSession
import com.calovelo.app.domain.model.WorkoutSource
import com.calovelo.app.domain.model.toLatLng
import com.calovelo.app.domain.util.PolylineEncoder
import java.util.UUID

fun RecordedActivity.toEntity(): WorkoutSessionEntity = WorkoutSessionEntity(
    id = UUID.randomUUID().toString(),
    source = WorkoutSource.APP_GPS,
    activityType = activityType.name,
    startTime = startTimeMillis,
    endTime = endTimeMillis,
    durationSeconds = durationSeconds,
    distanceMeters = distanceMeters,
    caloriesBurned = caloriesBurned,
    avgHeartRate = null,
    routePolyline = route
        .takeIf { it.size >= 2 }
        ?.let { points -> PolylineEncoder.encode(points.map(LatLng::toPair)) },
    rpeScore = null,
    feelingTag = null,
    userNotes = null,
    isSharedToClub = false
)

fun WorkoutSessionEntity.toDomain(): WorkoutSession = WorkoutSession(
    id = id,
    source = source,
    activityType = activityType,
    startTime = startTime,
    endTime = endTime,
    durationSeconds = durationSeconds,
    distanceMeters = distanceMeters,
    caloriesBurned = caloriesBurned,
    avgHeartRate = avgHeartRate,
    route = routePolyline?.let { encoded ->
        try {
            PolylineEncoder.decode(encoded).map { it.toLatLng() }
        } catch (e: IllegalArgumentException) {
            emptyList()
        }
    } ?: emptyList(),
    rpeScore = rpeScore,
    feelingTag = FeelingTag.fromName(feelingTag),
    userNotes = userNotes,
    isSharedToClub = isSharedToClub
)
