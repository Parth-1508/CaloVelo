package com.calovelo.app.presentation.dashboard

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.text.format.DateUtils
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calovelo.app.domain.model.WearableAccess
import java.text.NumberFormat

private const val HEALTH_CONNECT_PACKAGE = "com.google.android.apps.healthdata"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onStartWorkout: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val healthPermissionLauncher = rememberLauncherForActivityResult(viewModel.permissionContract) { granted ->
        viewModel.onPermissionsResult(granted)
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onNotificationPermissionResult(granted)
    }

    LifecycleEventEffect(Lifecycle.Event.ON_START) { viewModel.onScreenStarted() }

    LaunchedEffect(state.userMessage) {
        state.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onMessageShown()
        }
    }

    val onSyncClick: () -> Unit = {
        when (state.wearable.access) {
            WearableAccess.PERMISSION_REQUIRED -> healthPermissionLauncher.launch(viewModel.requiredPermissions)
            WearableAccess.PROVIDER_INSTALL_OR_UPDATE -> openHealthConnectInStore(context)
            else -> viewModel.onSyncClicked()
        }
    }

    val onRemindersToggled: (Boolean) -> Unit = { enable ->
        val needsRuntimePermission = enable &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        if (needsRuntimePermission) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            viewModel.onRemindersToggled(enable)
        }
    }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("CaloVelo") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                WorkoutCard(onStartWorkout = onStartWorkout)
                WearableSyncCard(state = state.wearable, onSyncClick = onSyncClick)
                HydrationCard(
                    state = state.hydration,
                    onQuickAdd = viewModel::onQuickAddWater,
                    onRemindersToggled = onRemindersToggled
                )
                Spacer(Modifier.size(8.dp))
            }
        }
    }
}

@Composable
private fun WorkoutCard(onStartWorkout: () -> Unit, modifier: Modifier = Modifier) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Filled.DirectionsRun, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Workout", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            Text(
                "Record a GPS run, ride or walk with live pace, route and calories.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onStartWorkout, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.DirectionsWalk, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text("Start Workout")
            }
        }
    }
}

@Composable
private fun WearableSyncCard(
    state: WearableCardState,
    onSyncClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val numberFormat = remember { NumberFormat.getIntegerInstance() }

    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Filled.Watch, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Wearables", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatTile(
                    icon = Icons.Filled.DirectionsWalk,
                    value = numberFormat.format(state.steps),
                    label = "Steps",
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    icon = Icons.Filled.LocalFireDepartment,
                    value = "${numberFormat.format(state.activeCaloriesKcal)} kcal",
                    label = "Active burn",
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    icon = Icons.Filled.Favorite,
                    value = state.heartRateBpm?.let { "$it bpm" } ?: "--",
                    label = "Heart rate",
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = syncStatusText(state),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = onSyncClick,
                enabled = !state.isSyncing && state.access != WearableAccess.UNAVAILABLE,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Filled.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.size(8.dp))
                Text(
                    when (state.access) {
                        WearableAccess.PERMISSION_REQUIRED -> "Grant Health Connect Access"
                        WearableAccess.PROVIDER_INSTALL_OR_UPDATE -> "Get Health Connect"
                        else -> "Sync Wearables"
                    }
                )
            }
        }
    }
}

@Composable
private fun StatTile(icon: ImageVector, value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun HydrationCard(
    state: HydrationCardState,
    onQuickAdd: (Int) -> Unit,
    onRemindersToggled: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = state.progress,
        animationSpec = tween(durationMillis = 600),
        label = "hydrationProgress"
    )

    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Filled.WaterDrop, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Hydration", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }

            Box(
                modifier = Modifier
                    .size(176.dp)
                    .semantics {
                        contentDescription =
                            "${state.percent} percent of daily water goal, ${state.intakeMl} of ${state.goalMl} milliliters"
                    },
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 14.dp,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${state.percent}%",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${state.intakeMl} / ${state.goalMl} ml",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilledTonalButton(onClick = { onQuickAdd(250) }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(4.dp))
                    Text("250 ml")
                }
                FilledTonalButton(onClick = { onQuickAdd(500) }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(4.dp))
                    Text("500 ml")
                }
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f).padding(end = 12.dp)) {
                    Text("Smart reminders", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Nudges between 8 AM and 10 PM when you fall behind pace",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = state.remindersEnabled, onCheckedChange = onRemindersToggled)
            }
        }
    }
}

private fun syncStatusText(state: WearableCardState): String = when {
    state.isSyncing -> "Syncing with Health Connect…"
    state.access == WearableAccess.PERMISSION_REQUIRED ->
        "Grant Health Connect access to see your wearable data."
    state.access == WearableAccess.PROVIDER_INSTALL_OR_UPDATE ->
        "Health Connect needs to be installed or updated."
    state.access == WearableAccess.UNAVAILABLE ->
        "Health Connect isn't supported on this device."
    state.lastSyncedTimestamp > 0L -> {
        val relative = DateUtils.getRelativeTimeSpanString(
            state.lastSyncedTimestamp,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        )
        "Last synced $relative"
    }
    else -> "Not synced yet."
}

private fun openHealthConnectInStore(context: Context) {
    val storeIntent = Intent(Intent.ACTION_VIEW).apply {
        setPackage("com.android.vending")
        data = Uri.parse("market://details?id=$HEALTH_CONNECT_PACKAGE&url=healthconnect%3A%2F%2Fonboarding")
        putExtra("overlay", true)
        putExtra("callerId", context.packageName)
    }
    try {
        context.startActivity(storeIntent)
    } catch (e: ActivityNotFoundException) {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$HEALTH_CONNECT_PACKAGE")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
