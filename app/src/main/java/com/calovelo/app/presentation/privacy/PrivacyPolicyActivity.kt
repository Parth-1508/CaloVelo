package com.calovelo.app.presentation.privacy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.calovelo.app.presentation.theme.CaloveloTheme

/**
 * Target of Health Connect's permission-rationale intent and the Android 14+ permission-usage
 * alias. Replace the copy below with your counsel-approved policy and a link to the hosted version.
 */
class PrivacyPolicyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CaloveloTheme {
                PrivacyPolicyScreen(onBack = ::finish)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrivacyPolicyScreen(onBack: () -> Unit) {
    Scaffold(topBar = { CenterAlignedTopAppBar(title = { Text("Privacy & Health Data") }) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Section(
                "What we read",
                "With your permission, CaloVelo reads your steps, heart rate, active calories burned, " +
                    "exercise sessions and sleep from Health Connect. CaloVelo never writes to Health Connect."
            )
            Section(
                "Location",
                "When you record a run, ride or walk, CaloVelo uses your precise location to measure " +
                    "distance, pace and draw your route. Location is only collected while a workout is " +
                    "being recorded, and a persistent notification is shown the entire time."
            )
            Section(
                "Why we read it",
                "This data powers your daily dashboard and workout history so you can see your activity " +
                    "next to your hydration progress."
            )
            Section(
                "Where it is stored",
                "Your data, including recorded routes (stored as an encoded path), is stored only on this " +
                    "device in a local database. It is not uploaded, sold or shared with third parties or " +
                    "used for advertising."
            )
            Section(
                "Your control",
                "You can revoke any permission at any time in Health Connect or system settings. " +
                    "Uninstalling CaloVelo removes its local data."
            )
            TextButton(onClick = onBack) { Text("Close") }
        }
    }
}

@Composable
private fun Section(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(body, style = MaterialTheme.typography.bodyMedium)
    }
}
