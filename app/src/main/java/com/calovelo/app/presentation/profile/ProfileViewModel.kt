package com.calovelo.app.presentation.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calovelo.app.domain.model.DietaryFilter
import com.calovelo.app.domain.model.WorkoutSession
import com.calovelo.app.domain.repository.SettingsRepository
import com.calovelo.app.domain.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class ProfileUiState(
    val userName: String = "Athlete",
    val avatarUri: String? = null,
    val ageYears: Int = 25,
    val heightCm: Int = 175,
    val weightKg: Float = 70f,
    val targetWeightKg: Float = 70f,
    val bioText: String = "My desire is to run",
    val locationString: String = "San Francisco, CA",
    val dietaryFilter: DietaryFilter = DietaryFilter.ALL,
    val bmi: Float = 22.9f,
    val weeklyDistanceKm: Float = 0f,
    val weeklyTimeSeconds: Long = 0L,
    val totalActivitiesCount: Int = 0,
    val weeklyStreak: Int = 3,
    val workouts: List<WorkoutSession> = emptyList()
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    val uiState: StateFlow<ProfileUiState> = combine(
        combine(
            settingsRepository.userName,
            settingsRepository.avatarUri,
            settingsRepository.ageYears,
            settingsRepository.heightCm,
            settingsRepository.weightKg
        ) { name, avatar, age, height, weight ->
            ProfileBase(name, avatar, age, height, weight)
        },
        combine(
            settingsRepository.targetWeightKg,
            settingsRepository.bioText,
            settingsRepository.locationString,
            settingsRepository.dietaryFilter
        ) { targetW, bio, loc, diet ->
            ProfileExtra(targetW, bio, loc, diet)
        },
        workoutRepository.observeWorkouts()
    ) { base, extra, workouts ->
        val hMeters = base.height / 100f
        val calculatedBmi = if (hMeters > 0f) base.weight / (hMeters * hMeters) else 0f

        // Calculate this week's distance and duration
        val zone = ZoneId.systemDefault()
        val now = LocalDate.now(zone)
        val startOfWeek = now.minusDays(now.dayOfWeek.value.toLong() - 1)
        val startOfWeekMillis = startOfWeek.atStartOfDay(zone).toInstant().toEpochMilli()

        val weekWorkouts = workouts.filter { it.startTime >= startOfWeekMillis }
        val weekDistMeters = weekWorkouts.sumOf { it.distanceMeters }
        val weekTimeSec = weekWorkouts.sumOf { it.durationSeconds }

        ProfileUiState(
            userName = base.name,
            avatarUri = base.avatar,
            ageYears = base.age,
            heightCm = base.height,
            weightKg = base.weight,
            targetWeightKg = extra.targetWeight,
            bioText = extra.bio,
            locationString = extra.location,
            dietaryFilter = extra.diet,
            bmi = calculatedBmi,
            weeklyDistanceKm = (weekDistMeters / 1000.0).toFloat(),
            weeklyTimeSeconds = weekTimeSec,
            totalActivitiesCount = workouts.size,
            weeklyStreak = calculateWeeklyStreak(workouts, zone),
            workouts = workouts
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProfileUiState()
    )

    fun onAvatarSelected(sourceUri: Uri) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val profileDir = File(context.filesDir, "profile").apply { if (!exists()) mkdirs() }
                    val destFile = File(profileDir, "profile_avatar.jpg")

                    context.contentResolver.openInputStream(sourceUri)?.use { input ->
                        FileOutputStream(destFile).use { output ->
                            input.copyTo(output)
                        }
                    }

                    val persistentUri = Uri.fromFile(destFile).toString()
                    settingsRepository.setAvatarUri(persistentUri)
                    _userMessage.value = "Profile picture updated successfully!"
                } catch (e: Exception) {
                    _userMessage.value = "Failed to save profile picture: ${e.message}"
                }
            }
        }
    }

    fun updateProfile(
        name: String,
        age: Int,
        height: Int,
        weight: Float,
        targetWeight: Float,
        bio: String,
        location: String
    ) {
        viewModelScope.launch {
            settingsRepository.setUserName(name)
            settingsRepository.setAgeYears(age)
            settingsRepository.setHeightCm(height)
            settingsRepository.setWeightKg(weight)
            settingsRepository.setTargetWeightKg(targetWeight)
            settingsRepository.setBioText(bio)
            settingsRepository.setLocationString(location)
            _userMessage.value = "Profile details saved."
        }
    }

    fun onDismissMessage() {
        _userMessage.value = null
    }

    private fun calculateWeeklyStreak(workouts: List<WorkoutSession>, zone: ZoneId): Int {
        if (workouts.isEmpty()) return 0
        val workoutWeeks = workouts.map {
            val date = Instant.ofEpochMilli(it.startTime).atZone(zone).toLocalDate()
            date.year * 53 + date.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR)
        }.distinct().sortedDescending()

        val currentWeek = LocalDate.now(zone).let {
            it.year * 53 + it.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR)
        }

        var streak = 0
        var expectedWeek = currentWeek
        for (w in workoutWeeks) {
            if (w == expectedWeek || w == expectedWeek - 1) {
                streak++
                expectedWeek = w - 1
            } else {
                break
            }
        }
        return maxOf(streak, 1)
    }
}

private data class ProfileBase(
    val name: String,
    val avatar: String?,
    val age: Int,
    val height: Int,
    val weight: Float
)

private data class ProfileExtra(
    val targetWeight: Float,
    val bio: String,
    val location: String,
    val diet: DietaryFilter
)
