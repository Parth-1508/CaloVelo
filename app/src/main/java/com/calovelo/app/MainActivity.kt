package com.calovelo.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.calovelo.app.presentation.navigation.CaloveloNavHost
import com.calovelo.app.presentation.navigation.Routes
import com.calovelo.app.presentation.theme.CaloveloTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var pendingRoute by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        pendingRoute = routeFrom(intent)
        setContent {
            CaloveloTheme {
                CaloveloNavHost(
                    pendingRoute = pendingRoute,
                    onPendingRouteConsumed = {
                        pendingRoute = null
                        // Prevents re-navigating after a configuration change.
                        intent?.removeExtra(EXTRA_OPEN_ROUTE)
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingRoute = routeFrom(intent)
    }

    private fun routeFrom(intent: Intent?): String? =
        intent?.getStringExtra(EXTRA_OPEN_ROUTE)?.takeIf { it == Routes.TRACKING }

    companion object {
        const val EXTRA_OPEN_ROUTE = "extra_open_route"
    }
}
