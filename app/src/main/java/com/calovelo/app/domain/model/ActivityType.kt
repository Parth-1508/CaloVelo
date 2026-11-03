package com.calovelo.app.domain.model

enum class ActivityType(
    val displayName: String,
    val selectorLabel: String,
    /** Fixes implying a faster speed between two samples are treated as GPS glitches. */
    val maxPlausibleSpeedMps: Double
) {
    RUNNING("Running", "Running", 12.0),
    OUTDOOR_CYCLING("Outdoor Cycling", "Cycling", 30.0),
    WALKING("Walking", "Walking", 6.0);

    companion object {
        fun fromName(name: String?): ActivityType = entries.firstOrNull { it.name == name } ?: RUNNING
    }
}
