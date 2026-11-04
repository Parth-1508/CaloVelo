// app/src/main/java/com/calovelo/app/presentation/nutrition/camera/FoodScannerUiState.kt
package com.calovelo.app.presentation.scanner

import com.calovelo.app.domain.model.BoundingBox
import com.calovelo.app.domain.model.FoodItem
import com.calovelo.app.domain.model.Macros
import com.calovelo.app.domain.model.MealType
import com.calovelo.app.domain.nutrition.MacroCalculator

enum class ScannerPhase {
    /** Live viewfinder. */
    LIVE,

    /** Photo taken, detector running. */
    ANALYZING,

    /** Showing the photo (with boxes, if any). */
    REVIEW
}

sealed interface ScanStatus {
    data object Idle : ScanStatus
    data class Detected(val count: Int) : ScanStatus
    data class NothingDetected(val unmappedLabels: List<String>) : ScanStatus
    data class ModelUnavailable(val reason: String) : ScanStatus
    data class Failed(val message: String) : ScanStatus
}

/** A food on the "plate" the user is about to save. [box] and [confidence] are null for foods added by search. */
data class ScanItemUi(
    val id: Int,
    val food: FoodItem,
    val grams: Int,
    val box: BoundingBox? = null,
    val confidence: Float? = null
) {
    val macros: Macros get() = MacroCalculator.macrosFor(food, grams.toDouble())
}

data class FoodScannerUiState(
    val phase: ScannerPhase = ScannerPhase.LIVE,
    /** A File or Uri that Coil can load; null while the viewfinder is live. */
    val capturedImage: Any? = null,
    /** Width / height of the upright photo; set only when boxes can be drawn over it. */
    val imageAspectRatio: Float? = null,
    val status: ScanStatus = ScanStatus.Idle,
    val items: List<ScanItemUi> = emptyList(),
    val mealType: MealType = MealType.LUNCH,
    val searchQuery: String = "",
    val searchResults: List<FoodItem> = emptyList(),
    val sheetVisible: Boolean = false,
    val isSaving: Boolean = false,
    /** True once the meals were saved; the screen reacts by navigating back. */
    val saved: Boolean = false,
    val userMessage: String? = null
) {
    val totalMacros: Macros get() = items.fold(Macros.ZERO) { acc, item -> acc + item.macros }
}
