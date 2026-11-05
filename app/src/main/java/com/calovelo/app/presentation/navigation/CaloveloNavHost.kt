package com.calovelo.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.calovelo.app.presentation.main.MainViewModel
import com.calovelo.app.presentation.theme.CaloTheme
import com.calovelo.app.presentation.theme.CaloThemeId
import com.calovelo.app.presentation.tracking.ActiveWorkoutScreen

object Routes {
    const val MAIN = "main"
    const val TRACKING = "tracking"
}

/**
 * Top-level nav host: switches between the main tabbed scaffold and the full-screen tracking screen.
 */
@Composable
fun CaloveloNavHost(
    navController: NavHostController = rememberNavController(),
    mainViewModel: MainViewModel = hiltViewModel(),
    onOpenPrivacyPolicy: () -> Unit = {}
) {
    val state by mainViewModel.uiState.collectAsStateWithLifecycle()

    // Wrap in CaloTheme so ALL composables inside get themed tokens
    CaloTheme(themeId = state.themeId) {
        NavHost(navController = navController, startDestination = Routes.MAIN) {
            composable(Routes.MAIN) {
                MainScaffold(
                    currentThemeId = state.themeId,
                    remindersEnabled = state.remindersEnabled,
                    userName = state.userName,
                    heightCm = state.heightCm,
                    weightKg = state.weightKg,
                    dailyCalorieGoal = state.dailyCalorieGoal,
                    dailyWaterGoalMl = state.dailyWaterGoalMl,
                    weeklyDistanceGoalKm = state.weeklyDistanceGoalKm,
                    waterIntakeMl = state.waterIntakeMl,
                    onThemeSelected = mainViewModel::setTheme,
                    onRemindersToggled = mainViewModel::setReminders,
                    onSaveProfile = mainViewModel::saveProfile,
                    onSaveGoals = mainViewModel::saveGoals,
                    onQuickAddWater = mainViewModel::quickAddWater,
                    onStartWorkout = {
                        navController.navigate(Routes.TRACKING) { launchSingleTop = true }
                    },
                    onOpenPrivacyPolicy = onOpenPrivacyPolicy
                )
            }
            composable(Routes.TRACKING) {
                ActiveWorkoutScreen(onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}
