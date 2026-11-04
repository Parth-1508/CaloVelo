// app/src/main/java/com/calovelo/app/domain/nutrition/FoodLabelKey.kt
package com.calovelo.app.domain.nutrition

import java.util.Locale

/** Normalizes a line of food_labels.txt ("Dal Tadka", "dal-tadka") to a catalog `labelKey` ("dal_tadka"). */
object FoodLabelKey {
    private val NON_ALPHANUMERIC = Regex("[^a-z0-9]+")

    fun fromLabel(label: String): String =
        label.trim().lowercase(Locale.ROOT).replace(NON_ALPHANUMERIC, "_").trim('_')
}
