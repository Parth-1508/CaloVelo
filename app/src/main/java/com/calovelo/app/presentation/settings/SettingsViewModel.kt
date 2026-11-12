package com.calovelo.app.presentation.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calovelo.app.data.local.CaloveloDatabase
import com.calovelo.app.data.local.seed.FoodCatalogSeeder
import com.calovelo.app.data.local.seed.GymCatalogSeeder
import com.calovelo.app.domain.model.DietaryFilter
import com.calovelo.app.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: CaloveloDatabase,
    private val foodCatalogSeeder: FoodCatalogSeeder,
    private val gymCatalogSeeder: GymCatalogSeeder,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val dietaryFilter: StateFlow<DietaryFilter> = settingsRepository.dietaryFilter
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DietaryFilter.ALL
        )

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    fun onDietaryFilterSelected(filter: DietaryFilter) {
        viewModelScope.launch {
            settingsRepository.setDietaryFilter(filter)
            _statusMessage.value = "Dietary preference updated to ${filter.displayName}"
        }
    }

    fun onReseedCatalog() {
        viewModelScope.launch {
            try {
                foodCatalogSeeder.forceReseed()
                gymCatalogSeeder.forceReseed()
                _statusMessage.value = "Food & Gym catalogs successfully re-seeded!"
            } catch (e: Exception) {
                _statusMessage.value = "Failed to re-seed catalog: ${e.message}"
            }
        }
    }

    fun onClearCache() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    context.cacheDir.deleteRecursively()
                    _statusMessage.value = "Application cache cleared successfully."
                } catch (e: Exception) {
                    _statusMessage.value = "Failed to clear cache: ${e.message}"
                }
            }
        }
    }

    fun onResetUserData() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    database.clearAllTables()
                    foodCatalogSeeder.forceReseed()
                    gymCatalogSeeder.forceReseed()
                    _statusMessage.value = "All user logs reset. Catalogs refreshed."
                } catch (e: Exception) {
                    _statusMessage.value = "Failed to reset data: ${e.message}"
                }
            }
        }
    }

    fun onDismissMessage() {
        _statusMessage.value = null
    }
}
