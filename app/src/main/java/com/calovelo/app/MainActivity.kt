package com.calovelo.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.calovelo.app.presentation.navigation.CaloveloNavHost
import com.calovelo.app.presentation.privacy.PrivacyPolicyActivity
import com.calovelo.app.presentation.theme.CaloveloTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        const val EXTRA_OPEN_ROUTE = "extra_open_route"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CaloveloTheme {
                CaloveloNavHost(
                    onOpenPrivacyPolicy = {
                        startActivity(Intent(this@MainActivity, PrivacyPolicyActivity::class.java))
                    }
                )
            }
        }
    }
}
