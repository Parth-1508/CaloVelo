package com.calovelo.app.domain.model

enum class DietaryFilter(
    val displayName: String,
    val badge: String
) {
    ALL("All Foods", "🌱🍗 All"),
    VEG("Vegetarian", "🌱 Veg"),
    NON_VEG("Non-Veg", "🍗 Non-Veg"),
    EGG_ONLY("Eggetarian", "🥚 Eggs OK"),
    VEGAN("Vegan", "🌿 Vegan"),
    LACTOSE_INTOLERANT("Lactose-Free", "🥛 Lactose-Free");

    companion object {
        fun fromName(name: String?): DietaryFilter =
            entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: ALL
    }
}
