package com.calovelo.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.calovelo.app.presentation.dashboard.DashboardScreen
import com.calovelo.app.presentation.tracking.ActiveWorkoutScreen

object Routes {
    const val DASHBOARD = "dashboard"
    const val TRACKING = "tracking"
}

/**
 * @param pendingRoute a route requested from outside the UI (e.g. a notification tap); it is
 * navigated to once and then reported through [onPendingRouteConsumed].
 */
@Composable
fun CaloveloNavHost(
    navController: NavHostController = rememberNavController(),
    pendingRoute: String? = null,
    onPendingRouteConsumed: () -> Unit = {}
) {
    LaunchedEffect(pendingRoute) {
        if (pendingRoute != null) {
            navController.navigate(pendingRoute) { launchSingleTop = true }
            onPendingRouteConsumed()
        }
    }

    NavHost(navController = navController, startDestination = Routes.DASHBOARD) {
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onStartWorkout = {
                    navController.navigate(Routes.TRACKING) { launchSingleTop = true }
                }
            )
        }
        composable(Routes.TRACKING) {
            ActiveWorkoutScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
