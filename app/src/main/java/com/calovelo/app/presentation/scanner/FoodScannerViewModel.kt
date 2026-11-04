// app/src/main/java/com/calovelo/app/presentation/nutrition/camera/FoodScannerViewModel.kt
package com.calovelo.app.presentation.scanner

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calovelo.app.domain.model.FoodItem
import com.calovelo.app.domain.model.FoodScanResult
import com.calovelo.app.domain.model.ImageSource
import com.calovelo.app.domain.model.MealEntry
import com.calovelo.app.domain.model.MealSource
import com.calovelo.app.domain.model.MealType
import com.calovelo.app.domain.repository.NutritionRepository
import com.calovelo.app.domain.usecase.LogMealsUseCase
import com.calovelo.app.domain.usecase.ScanFoodUseCase
import com.calovelo.app.domain.usecase.SearchFoodsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class FoodScannerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val scanFood: ScanFoodUseCase,
    private val searchFoods: SearchFoodsUseCase,
    private val logMeals: LogMealsUseCase,
    clock: Clock
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        FoodScannerUiState(
            // "mealType" is the optional navigation argument; otherwise guess from the time of day.
            mealType = MealType.fromName(savedStateHandle.get<String>(ARG_MEAL_TYPE))
                ?: MealType.suggestedFor(LocalTime.now(clock))
        )
    )
    val uiState: StateFlow<FoodScannerUiState> = _uiState.asStateFlow()

    private val query = MutableStateFlow("")
    private var nextItemId = 0
    private var scanJob: Job? = null

    init {
        observeSearch()
    }

    // ---- Capture & detection ----------------------------------------------------------------

    /**
     * A photo is available (camera or gallery). [previewModel] is what the UI shows for it (a File
     * or Uri); [source] is read once to run detection.
     */
    fun onPhotoReady(source: ImageSource, previewModel: Any) {
        scanJob?.cancel()
        _uiState.update {
            it.copy(
                phase = ScannerPhase.ANALYZING,
                capturedImage = previewModel,
                imageAspectRatio = null,
                status = ScanStatus.Idle,
                items = emptyList(),
                sheetVisible = false
            )
        }
        scanJob = viewModelScope.launch {
            val result = try {
                scanFood(source)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                FoodScanResult.Failure("Couldn't analyse that photo. Please try again.")
            }
            applyScanResult(result)
        }
    }

    fun onCaptureFailed() {
        _uiState.update { it.copy(userMessage = "Couldn't take the photo. Please try again.") }
    }

    fun onRetake() {
        scanJob?.cancel()
        _uiState.update {
            it.copy(
                phase = ScannerPhase.LIVE,
                capturedImage = null,
                imageAspectRatio = null,
                status = ScanStatus.Idle,
                items = emptyList(),
                sheetVisible = false
            )
        }
    }

    private fun applyScanResult(result: FoodScanResult) {
        _uiState.update { state ->
            when (result) {
                is FoodScanResult.Detected -> state.copy(
                    imageAspectRatio = result.aspectRatio,
                    status = ScanStatus.Detected(result.candidates.size),
                    items = result.candidates.map { candidate ->
                        ScanItemUi(
                            id = nextItemId++,
                            food = candidate.food,
                            grams = candidate.suggestedGrams.coerceIn(MIN_GRAMS, NutritionRepository.MAX_GRAMS),
                            box = candidate.box,
                            confidence = candidate.confidence
                        )
                    }
                )
                is FoodScanResult.NothingDetected -> state.copy(status = ScanStatus.NothingDetected(result.unmappedLabels))
                is FoodScanResult.ModelUnavailable -> state.copy(status = ScanStatus.ModelUnavailable(result.reason))
                is FoodScanResult.Failure -> state.copy(status = ScanStatus.Failed(result.message))
            }.copy(phase = ScannerPhase.REVIEW, sheetVisible = true)
        }
    }

    // ---- Editing the plate ------------------------------------------------------------------

    fun onOpenSheet() = _uiState.update { it.copy(sheetVisible = true) }

    fun onSheetDismissed() = _uiState.update { it.copy(sheetVisible = false) }

    fun onMealTypeSelected(type: MealType) = _uiState.update { it.copy(mealType = type) }

    fun onSearchQueryChanged(text: String) {
        _uiState.update { it.copy(searchQuery = text) }
        query.value = text
    }

    fun onFoodPicked(food: FoodItem) {
        _uiState.update { state ->
            state.copy(items = state.items + ScanItemUi(nextItemId++, food, food.defaultServingGrams))
        }
    }

    fun onGramsChanged(itemId: Int, grams: Int) {
        val clamped = grams.coerceIn(MIN_GRAMS, NutritionRepository.MAX_GRAMS)
        _uiState.update { state ->
            state.copy(items = state.items.map { if (it.id == itemId) it.copy(grams = clamped) else it })
        }
    }

    fun onRemoveItem(itemId: Int) {
        _uiState.update { state -> state.copy(items = state.items.filterNot { it.id == itemId }) }
    }

    fun onSave() {
        val state = _uiState.value
        if (state.isSaving || state.saved) return
        if (state.items.isEmpty()) {
            _uiState.update { it.copy(userMessage = "Add at least one food first.") }
            return
        }
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                logMeals(
                    entries = state.items.map { item ->
                        MealEntry(
                            food = item.food,
                            grams = item.grams,
                            source = if (item.confidence != null) MealSource.SCANNER else MealSource.MANUAL
                        )
                    },
                    mealType = state.mealType
                )
                _uiState.update { it.copy(isSaving = false, saved = true) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, userMessage = "Couldn't save your meal. Please try again.") }
            }
        }
    }

    fun onMessageShown() = _uiState.update { it.copy(userMessage = null) }

    // ---- Catalog search ---------------------------------------------------------------------

    @OptIn(FlowPreview::class)
    private fun observeSearch() {
        viewModelScope.launch {
            query
                .debounce(SEARCH_DEBOUNCE_MS)
                .distinctUntilChanged()
                .collectLatest { text ->
                    try {
                        val results = searchFoods(text)
                        _uiState.update { it.copy(searchResults = results) }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        _uiState.update { it.copy(userMessage = "Couldn't search the food catalog.") }
                    }
                }
        }
    }

    companion object {
        const val ARG_MEAL_TYPE = "mealType"
        private const val MIN_GRAMS = 5
        private const val SEARCH_DEBOUNCE_MS = 200L
    }
}
