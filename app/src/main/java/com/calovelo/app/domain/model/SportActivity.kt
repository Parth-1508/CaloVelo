package com.calovelo.app.domain.model

enum class SportCategory(val displayName: String) {
    TOP("Your Top Sports"),
    FOOT("Foot Sports"),
    CYCLE("Cycle Sports"),
    STRENGTH("Strength"),
    RACKET("Racket Sports"),
    WATER("Water Sports"),
    WINTER("Winter Sports"),
    TEAM("Team Sports"),
    OTHER("Other Sports")
}

enum class SportTrackingType {
    GPS_CARDIO,
    GYM_STRENGTH
}

data class SportActivity(
    val id: String,
    val displayName: String,
    val category: SportCategory,
    val trackingType: SportTrackingType = SportTrackingType.GPS_CARDIO
)

object StravaSportsCatalog {
    val RUN = SportActivity("run", "Run", SportCategory.FOOT, SportTrackingType.GPS_CARDIO)
    val TRAIL_RUN = SportActivity("trail_run", "Trail Run", SportCategory.FOOT, SportTrackingType.GPS_CARDIO)
    val WALK = SportActivity("walk", "Walk", SportCategory.FOOT, SportTrackingType.GPS_CARDIO)
    val HIKE = SportActivity("hike", "Hike", SportCategory.FOOT, SportTrackingType.GPS_CARDIO)
    val WHEELCHAIR = SportActivity("wheelchair", "Wheelchair", SportCategory.FOOT, SportTrackingType.GPS_CARDIO)

    val RIDE = SportActivity("ride", "Ride", SportCategory.CYCLE, SportTrackingType.GPS_CARDIO)
    val MOUNTAIN_BIKE = SportActivity("mountain_bike", "Mountain Bike Ride", SportCategory.CYCLE, SportTrackingType.GPS_CARDIO)
    val GRAVEL_RIDE = SportActivity("gravel_ride", "Gravel Ride", SportCategory.CYCLE, SportTrackingType.GPS_CARDIO)
    val E_BIKE = SportActivity("e_bike", "E-Bike Ride", SportCategory.CYCLE, SportTrackingType.GPS_CARDIO)
    val E_MOUNTAIN_BIKE = SportActivity("e_mountain_bike", "E-Mountain Bike Ride", SportCategory.CYCLE, SportTrackingType.GPS_CARDIO)
    val HANDCYCLE = SportActivity("handcycle", "Handcycle", SportCategory.CYCLE, SportTrackingType.GPS_CARDIO)
    val VELOMOBILE = SportActivity("velomobile", "Velomobile", SportCategory.CYCLE, SportTrackingType.GPS_CARDIO)

    val WEIGHT_TRAINING = SportActivity("weight_training", "Weight Training", SportCategory.STRENGTH, SportTrackingType.GYM_STRENGTH)
    val WORKOUT = SportActivity("workout", "Workout", SportCategory.STRENGTH, SportTrackingType.GYM_STRENGTH)
    val HIIT = SportActivity("hiit", "HIIT", SportCategory.STRENGTH, SportTrackingType.GYM_STRENGTH)
    val CROSSFIT = SportActivity("crossfit", "Crossfit", SportCategory.STRENGTH, SportTrackingType.GYM_STRENGTH)
    val PHYSIOTHERAPY = SportActivity("physiotherapy", "Physiotherapy", SportCategory.STRENGTH, SportTrackingType.GYM_STRENGTH)

    val TENNIS = SportActivity("tennis", "Tennis", SportCategory.RACKET, SportTrackingType.GPS_CARDIO)
    val PADEL = SportActivity("padel", "Padel", SportCategory.RACKET, SportTrackingType.GPS_CARDIO)
    val SQUASH = SportActivity("squash", "Squash", SportCategory.RACKET, SportTrackingType.GPS_CARDIO)
    val PICKLEBALL = SportActivity("pickleball", "Pickleball", SportCategory.RACKET, SportTrackingType.GPS_CARDIO)
    val BADMINTON = SportActivity("badminton", "Badminton", SportCategory.RACKET, SportTrackingType.GPS_CARDIO)
    val TABLE_TENNIS = SportActivity("table_tennis", "Table Tennis", SportCategory.RACKET, SportTrackingType.GPS_CARDIO)

    val ROWING = SportActivity("rowing", "Rowing", SportCategory.WATER, SportTrackingType.GPS_CARDIO)
    val CANOE = SportActivity("canoe", "Canoe", SportCategory.WATER, SportTrackingType.GPS_CARDIO)
    val SAILING = SportActivity("sailing", "Sailing", SportCategory.WATER, SportTrackingType.GPS_CARDIO)
    val KAYAKING = SportActivity("kayaking", "Kayaking", SportCategory.WATER, SportTrackingType.GPS_CARDIO)
    val STAND_UP_PADDLING = SportActivity("sup", "Stand Up Paddling", SportCategory.WATER, SportTrackingType.GPS_CARDIO)
    val SURFING = SportActivity("surfing", "Surfing", SportCategory.WATER, SportTrackingType.GPS_CARDIO)
    val SWIM = SportActivity("swim", "Swim", SportCategory.WATER, SportTrackingType.GPS_CARDIO)

    val ICE_SKATE = SportActivity("ice_skate", "Ice Skate", SportCategory.WINTER, SportTrackingType.GPS_CARDIO)
    val ALPINE_SKI = SportActivity("alpine_ski", "Alpine Ski", SportCategory.WINTER, SportTrackingType.GPS_CARDIO)
    val NORDIC_SKI = SportActivity("nordic_ski", "Nordic Ski", SportCategory.WINTER, SportTrackingType.GPS_CARDIO)
    val BACKCOUNTRY_SKI = SportActivity("backcountry_ski", "Backcountry Ski", SportCategory.WINTER, SportTrackingType.GPS_CARDIO)
    val SNOWBOARD = SportActivity("snowboard", "Snowboard", SportCategory.WINTER, SportTrackingType.GPS_CARDIO)
    val SNOWSHOE = SportActivity("snowshoe", "Snowshoe", SportCategory.WINTER, SportTrackingType.GPS_CARDIO)

    val FOOTBALL = SportActivity("football", "Football (Soccer)", SportCategory.TEAM, SportTrackingType.GPS_CARDIO)
    val BASKETBALL = SportActivity("basketball", "Basketball", SportCategory.TEAM, SportTrackingType.GPS_CARDIO)
    val VOLLEYBALL = SportActivity("volleyball", "Volleyball", SportCategory.TEAM, SportTrackingType.GPS_CARDIO)
    val CRICKET = SportActivity("cricket", "Cricket", SportCategory.TEAM, SportTrackingType.GPS_CARDIO)

    val INLINE_SKATE = SportActivity("inline_skate", "Inline Skate", SportCategory.OTHER, SportTrackingType.GPS_CARDIO)
    val ROLLER_SKI = SportActivity("roller_ski", "Roller Ski", SportCategory.OTHER, SportTrackingType.GPS_CARDIO)
    val ROCK_CLIMB = SportActivity("rock_climb", "Rock Climb", SportCategory.OTHER, SportTrackingType.GYM_STRENGTH)
    val ELLIPTICAL = SportActivity("elliptical", "Elliptical", SportCategory.OTHER, SportTrackingType.GPS_CARDIO)
    val STAIR_STEPPER = SportActivity("stair_stepper", "Stair Stepper", SportCategory.OTHER, SportTrackingType.GPS_CARDIO)
    val YOGA = SportActivity("yoga", "Yoga", SportCategory.OTHER, SportTrackingType.GYM_STRENGTH)
    val DANCE = SportActivity("dance", "Dance", SportCategory.OTHER, SportTrackingType.GPS_CARDIO)
    val SKATEBOARDING = SportActivity("skateboarding", "Skateboarding", SportCategory.OTHER, SportTrackingType.GPS_CARDIO)
    val GOLF = SportActivity("golf", "Golf", SportCategory.OTHER, SportTrackingType.GPS_CARDIO)
    val PILATES = SportActivity("pilates", "Pilates", SportCategory.OTHER, SportTrackingType.GYM_STRENGTH)

    val allSports: List<SportActivity> = listOf(
        RUN, TRAIL_RUN, WALK, HIKE, WHEELCHAIR,
        RIDE, MOUNTAIN_BIKE, GRAVEL_RIDE, E_BIKE, E_MOUNTAIN_BIKE, HANDCYCLE, VELOMOBILE,
        WEIGHT_TRAINING, WORKOUT, HIIT, CROSSFIT, PHYSIOTHERAPY,
        TENNIS, PADEL, SQUASH, PICKLEBALL, BADMINTON, TABLE_TENNIS,
        ROWING, CANOE, SAILING, KAYAKING, STAND_UP_PADDLING, SURFING, SWIM,
        ICE_SKATE, ALPINE_SKI, NORDIC_SKI, BACKCOUNTRY_SKI, SNOWBOARD, SNOWSHOE,
        FOOTBALL, BASKETBALL, VOLLEYBALL, CRICKET,
        INLINE_SKATE, ROLLER_SKI, ROCK_CLIMB, ELLIPTICAL, STAIR_STEPPER, YOGA, DANCE, SKATEBOARDING, GOLF, PILATES
    )

    fun findById(id: String?): SportActivity = allSports.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: RUN
}
