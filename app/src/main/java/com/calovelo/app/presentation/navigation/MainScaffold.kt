package com.calovelo.app.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.calovelo.app.presentation.crew.CrewContent
import com.calovelo.app.presentation.goals.GoalsContent
import com.calovelo.app.presentation.history.HistoryContent
import com.calovelo.app.presentation.home.HomeContent
import com.calovelo.app.presentation.hydration.HydrationContent
import com.calovelo.app.presentation.insights.InsightsContent
import com.calovelo.app.presentation.profile.ProfileScreen
import com.calovelo.app.presentation.scanner.ScannerContent
import com.calovelo.app.presentation.settings.SettingsScreen
import com.calovelo.app.presentation.theme.CaloTheme
import com.calovelo.app.presentation.theme.CaloThemeId
import com.calovelo.app.presentation.training.TrainingContent
import com.calovelo.app.presentation.workout.WorkoutContent
import com.calovelo.app.presentation.gym.GymTrackerContent

enum class BottomTab(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    Home("tab_home", "Home", Icons.Default.Home),
    Scanner("tab_scanner", "Scan", Icons.Default.CameraAlt),
    Training("tab_training", "Train", Icons.Default.FitnessCenter),
    Insights("tab_insights", "Insights", Icons.Default.Analytics),
    Settings("tab_settings", "Settings", Icons.Default.Settings)
}

@Composable
fun MainScaffold(
    currentThemeId: CaloThemeId,
    remindersEnabled: Boolean,
    userName: String,
    heightCm: Int,
    weightKg: Float,
    dailyCalorieGoal: Int,
    dailyWaterGoalMl: Int,
    weeklyDistanceGoalKm: Float,
    waterIntakeMl: Int,
    onThemeSelected: (CaloThemeId) -> Unit,
    onRemindersToggled: (Boolean) -> Unit,
    onSaveProfile: (name: String, height: Int, weight: Float) -> Unit,
    onSaveGoals: (calories: Int, waterMl: Int, distanceKm: Float) -> Unit,
    onQuickAddWater: (Int) -> Unit,
    onStartWorkout: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = CaloTheme.tokens
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Determine if we should show the bottom bar (hide on detail screens)
    val showBottomBar = BottomTab.entries.any { tab ->
        currentDestination?.hierarchy?.any { it.route == tab.route } == true
    } || currentDestination?.route == null // initial state

    Scaffold(
        containerColor = tokens.background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = tokens.surface,
                    contentColor = tokens.text
                ) {
                    BottomTab.entries.forEach { tab ->
                        val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(tab.icon, contentDescription = tab.label, tint = if (selected) tokens.energyMark else tokens.muted)
                            },
                            label = {
                                Text(
                                    tab.label,
                                    color = if (selected) tokens.energyMark else tokens.muted,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = (tokens.greenStateContainer ?: tokens.surface).copy(alpha = 0.6f)
                            )
                        )
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomTab.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // ── Bottom tabs ─────────────────────────
            composable(BottomTab.Home.route) {
                HomeContent(
                    userName = userName,
                    dailyCalorieGoal = dailyCalorieGoal,
                    dailyWaterGoalMl = dailyWaterGoalMl,
                    waterIntakeMl = waterIntakeMl,
                    onStartWorkout = onStartWorkout,
                    onOpenScanner = {
                        navController.navigate(BottomTab.Scanner.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onOpenTraining = {
                        navController.navigate(BottomTab.Training.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onOpenHydration = {
                        navController.navigate("hydration") { launchSingleTop = true }
                    },
                    onOpenHistory = {
                        navController.navigate("history") { launchSingleTop = true }
                    },
                    onOpenCrew = {
                        navController.navigate("crew") { launchSingleTop = true }
                    },
                    onOpenProfile = {
                        navController.navigate("profile") { launchSingleTop = true }
                    },
                    onOpenGoals = {
                        navController.navigate("goals") { launchSingleTop = true }
                    },
                    onQuickAddWater = onQuickAddWater
                )
            }
            composable(BottomTab.Scanner.route) {
                ScannerContent()
            }
            composable(BottomTab.Training.route) {
                GymTrackerContent()
            }
            composable(BottomTab.Insights.route) {
                InsightsContent(
                    onNavigateToScanner = {
                        navController.navigate(BottomTab.Scanner.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(BottomTab.Settings.route) {
                SettingsScreen(
                    currentThemeId = currentThemeId,
                    remindersEnabled = remindersEnabled,
                    onThemeSelected = onThemeSelected,
                    onRemindersToggled = onRemindersToggled,
                    onOpenProfile = { navController.navigate("profile") },
                    onOpenGoals = { navController.navigate("goals") },
                    onOpenPrivacyPolicy = onOpenPrivacyPolicy,
                    onSignOut = {} // TODO: wire actual sign-out
                )
            }

            // ── Detail screens (pushed on top, bottom bar hidden) ─────────
            composable("profile") {
                ProfileScreen(
                    userName = userName,
                    heightCm = heightCm,
                    weightKg = weightKg,
                    onSave = { name, h, w ->
                        onSaveProfile(name, h, w)
                        navController.popBackStack()
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("goals") {
                GoalsContent(
                    dailyCalorieGoal = dailyCalorieGoal,
                    dailyWaterGoalMl = dailyWaterGoalMl,
                    weeklyDistanceGoalKm = weeklyDistanceGoalKm,
                    onSave = { cal, water, dist ->
                        onSaveGoals(cal, water, dist)
                        navController.popBackStack()
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("history") {
                HistoryContent(onNavigateBack = { navController.popBackStack() })
            }
            composable("hydration") {
                HydrationContent(
                    intakeMl = waterIntakeMl,
                    goalMl = dailyWaterGoalMl,
                    onQuickAdd = onQuickAddWater,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("crew") {
                CrewContent(onNavigateBack = { navController.popBackStack() })
            }
            composable("workout") {
                WorkoutContent(
                    onStartWorkout = onStartWorkout,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("training_program") {
                TrainingContent(
                    onStartWorkout = onStartWorkout,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
