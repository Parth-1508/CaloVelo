package com.calovelo.app.presentation.workout

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calovelo.app.core.format.WorkoutFormatters
import com.calovelo.app.domain.model.ActivityType
import com.calovelo.app.domain.model.SportActivity
import com.calovelo.app.domain.model.SportCategory
import com.calovelo.app.domain.model.SportTrackingType
import com.calovelo.app.domain.model.StravaSportsCatalog
import com.calovelo.app.presentation.gym.GymTrackerContent
import com.calovelo.app.presentation.theme.CaloTheme
import com.calovelo.app.presentation.tracking.ActiveWorkoutViewModel
import com.calovelo.app.presentation.tracking.RpeFeedbackBottomSheet

@Composable
fun UnifiedWorkoutScreen(
    initialSportId: String = "run",
    modifier: Modifier = Modifier,
    activeWorkoutViewModel: ActiveWorkoutViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val tokens = CaloTheme.tokens
    val uiState by activeWorkoutViewModel.uiState.collectAsStateWithLifecycle()

    var selectedSport by remember(initialSportId) {
        mutableStateOf(StravaSportsCatalog.findById(initialSportId))
    }
    var showSportSelector by remember { mutableStateOf(false) }

    val stravaOrange = Color(0xFFFC4C02)
    val dockBackground = Color(0xFF000000)

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        if (fineLocationGranted) {
            val actType = when (selectedSport.category) {
                SportCategory.CYCLE -> ActivityType.OUTDOOR_CYCLING
                SportCategory.FOOT -> ActivityType.RUNNING
                else -> ActivityType.RUNNING
            }
            activeWorkoutViewModel.onActivitySelected(actType)
            activeWorkoutViewModel.onStartClicked()
        }
    }

    fun checkAndStartRecording() {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val notificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true

        if (fineLocation && notificationPermission) {
            val actType = when (selectedSport.category) {
                SportCategory.CYCLE -> ActivityType.OUTDOOR_CYCLING
                SportCategory.FOOT -> ActivityType.RUNNING
                else -> ActivityType.RUNNING
            }
            activeWorkoutViewModel.onActivitySelected(actType)
            activeWorkoutViewModel.onStartClicked()
        } else {
            val req = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                req.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            permissionLauncher.launch(req.toTypedArray())
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(tokens.background)
    ) {
        if (selectedSport.trackingType == SportTrackingType.GPS_CARDIO) {
            // ─────────────────────────────────────────────────────────────
            // GPS Cardio Mode (Live Map + HUD + Strava Dock)
            // ─────────────────────────────────────────────────────────────
            Column(modifier = Modifier.fillMaxSize()) {
                // Interactive Google Map with live glowing polyline & HUD
                LiveGpsMapView(
                    currentLocation = uiState.tracking.currentRoutePoints.lastOrNull(),
                    routePoints = uiState.tracking.currentRoutePoints,
                    elapsedTimeFormatted = WorkoutFormatters.formatElapsed(uiState.tracking.elapsedTimeSeconds),
                    splitPaceFormatted = WorkoutFormatters.formatPace(uiState.tracking.currentPaceSecondsPerKm),
                    distanceKmFormatted = WorkoutFormatters.formatDistanceKm(uiState.tracking.distanceMeters),
                    gpsAcquired = uiState.tracking.currentRoutePoints.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                    onBackClick = onNavigateBack
                )

                // Strava Bottom Dock (Matching screenshots 0, 1, etc.)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .background(dockBackground)
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .padding(horizontal = 24.dp, vertical = 14.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Top pull handle
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.Gray.copy(alpha = 0.5f))
                        )

                        // Three Primary Action Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left Action: Sport Selector Pill
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.clickable { showSportSelector = true }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(stravaOrange.copy(alpha = 0.18f))
                                        .border(1.5.dp, stravaOrange, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val icon = if (selectedSport.category == SportCategory.CYCLE) {
                                        Icons.Default.DirectionsBike
                                    } else {
                                        Icons.Default.DirectionsRun
                                    }
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = "Sport Selector",
                                        tint = stravaOrange,
                                        modifier = Modifier.size(28.dp)
                                    )

                                    // Orange checkmark badge
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(stravaOrange),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(10.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = selectedSport.displayName,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            // Center Action: Strava Orange Record / Pause / Stop
                            if (!uiState.tracking.isTracking) {
                                // Big Orange Play Button
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(stravaOrange)
                                        .clickable { checkAndStartRecording() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.PlayArrow,
                                        contentDescription = "Start Workout",
                                        tint = Color.White,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                            } else {
                                // Paused or Running controls
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    if (uiState.tracking.isPaused) {
                                        Box(
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(CircleShape)
                                                .background(stravaOrange)
                                                .clickable { activeWorkoutViewModel.onResumeClicked() },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = "Resume", tint = Color.White, modifier = Modifier.size(36.dp))
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFFFA000))
                                                .clickable { activeWorkoutViewModel.onPauseClicked() },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Pause, contentDescription = "Pause", tint = Color.Black, modifier = Modifier.size(32.dp))
                                        }
                                    }

                                    // Finish / Stop button
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFE53935))
                                            .clickable { activeWorkoutViewModel.onFinishClicked() },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Stop, contentDescription = "Finish", tint = Color.White, modifier = Modifier.size(32.dp))
                                    }
                                }
                            }

                            // Right Action: Add Route
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.clickable {
                                    // Placeholder for route import / gpx
                                }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.1f))
                                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.AltRoute,
                                        contentDescription = "Add Route",
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Text(
                                    text = "Add Route",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        // Bottom Share Link Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF141414))
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Share your location via text.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.1f))
                                    .clickable {
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(
                                                Intent.EXTRA_TEXT,
                                                "Tracking my ${selectedSport.displayName} workout with CaloVelo! Distance: ${WorkoutFormatters.formatDistanceKm(uiState.tracking.distanceMeters)} km"
                                            )
                                            type = "text/plain"
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "Share Location"))
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Share Link", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = stravaOrange)
                            }
                        }
                    }
                }
            }
        } else {
            // ─────────────────────────────────────────────────────────────
            // Gym / Strength Mode (Routine, Exercises, Sets, Animated Guides)
            // ─────────────────────────────────────────────────────────────
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Sport Mode Switcher Banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(tokens.surface.copy(alpha = 0.9f))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(stravaOrange.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = stravaOrange, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text(
                                text = selectedSport.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = tokens.text
                            )
                            Text(
                                text = "Strength & Gym Hub",
                                style = MaterialTheme.typography.bodySmall,
                                color = tokens.muted
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { showSportSelector = true },
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("Change Sport", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Gym Strength Tracker with exercise catalog, sets, rest timer
                GymTrackerContent(modifier = Modifier.weight(1f))
            }
        }

        // Post-Workout RPE Feedback Bottom Sheet
        uiState.feedback?.let { feedback ->
            RpeFeedbackBottomSheet(
                pending = feedback,
                isSaving = uiState.isSaving,
                onSave = { rpe, feeling, notes ->
                    activeWorkoutViewModel.onFeedbackSubmitted(rpe, feeling, notes)
                },
                onDismiss = {
                    activeWorkoutViewModel.onFeedbackDismissed()
                }
            )
        }

        // Strava Sports Category Taxonomy Bottom Sheet
        if (showSportSelector) {
            SportSelectorBottomSheet(
                selectedSport = selectedSport,
                onSportSelected = { sport ->
                    selectedSport = sport
                },
                onDismissRequest = {
                    showSportSelector = false
                }
            )
        }
    }
}
