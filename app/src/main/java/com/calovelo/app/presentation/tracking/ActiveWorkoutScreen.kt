package com.calovelo.app.presentation.tracking

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calovelo.app.core.format.WorkoutFormatters
import com.calovelo.app.domain.model.ActivityType
import com.calovelo.app.domain.model.LatLng
import com.calovelo.app.presentation.theme.CaloTheme
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(
    onNavigateBack: () -> Unit,
    viewModel: ActiveWorkoutViewModel = hiltViewModel()
) {
    val tokens = CaloTheme.tokens
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val view = LocalView.current
    val snackbarHostState = remember { SnackbarHostState() }
    val isTracking = state.tracking.isTracking

    // After the system dialog: start only if precise location was granted. A notification
    // permission denial alone does not block recording (the service still runs).
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        if (hasFineLocation(context)) viewModel.onStartClicked() else viewModel.onLocationPermissionDenied()
    }

    val onStartClick: () -> Unit = {
        val missing = requiredPermissions().filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isEmpty()) viewModel.onStartClicked() else permissionLauncher.launch(missing.toTypedArray())
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                ActiveWorkoutEvent.NavigateBack -> onNavigateBack()
            }
        }
    }

    LaunchedEffect(state.userMessage) {
        state.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onMessageShown()
        }
    }

    // Keep the screen awake only while a workout is being recorded.
    DisposableEffect(isTracking) {
        view.keepScreenOn = isTracking
        onDispose { view.keepScreenOn = false }
    }

    Scaffold(
        containerColor = tokens.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Workout", color = tokens.text, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = tokens.text)
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = tokens.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ActivitySelector(
                selected = state.displayedActivity,
                enabled = !isTracking,
                onSelected = viewModel::onActivitySelected,
                tokens = tokens
            )

            MetricsPanel(state = state, tokens = tokens)

            RoutePreview(
                points = state.tracking.currentRoutePoints,
                isTracking = isTracking,
                tokens = tokens,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .heightIn(min = 140.dp)
            )

            ControlButtons(
                isTracking = isTracking,
                isPaused = state.tracking.isPaused,
                tokens = tokens,
                onStart = onStartClick,
                onPause = viewModel::onPauseClicked,
                onResume = viewModel::onResumeClicked,
                onFinish = viewModel::onFinishClicked
            )
        }
    }

    state.feedback?.let { pending ->
        RpeFeedbackBottomSheet(
            pending = pending,
            isSaving = state.isSaving,
            onSave = viewModel::onFeedbackSubmitted,
            onDismiss = viewModel::onFeedbackDismissed
        )
    }
}

@Composable
private fun ActivitySelector(
    selected: ActivityType,
    enabled: Boolean,
    onSelected: (ActivityType) -> Unit,
    tokens: com.calovelo.app.presentation.theme.CaloTokens
) {
    val options = ActivityType.entries
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, type ->
            SegmentedButton(
                selected = type == selected,
                onClick = { onSelected(type) },
                enabled = enabled,
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = tokens.energyMark,
                    activeContentColor = tokens.onStart,
                    inactiveContainerColor = tokens.surface.copy(alpha = 0.5f),
                    inactiveContentColor = tokens.muted
                )
            ) {
                Text(type.selectorLabel, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun MetricsPanel(
    state: ActiveWorkoutUiState,
    tokens: com.calovelo.app.presentation.theme.CaloTokens
) {
    val tracking = state.tracking
    val elapsed = WorkoutFormatters.formatElapsed(tracking.elapsedTimeSeconds)
    val statusLabel = when {
        !tracking.isTracking -> "READY"
        tracking.isPaused -> "PAUSED"
        else -> "RECORDING"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(tokens.surface.copy(alpha = 0.95f))
            .border(1.dp, tokens.border.copy(alpha = 0.35f), RoundedCornerShape(28.dp))
            .padding(horizontal = 16.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(tokens.greenStateContainer ?: tokens.surface.copy(alpha = 0.5f))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = statusLabel,
                    color = tokens.energyMark,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }

            Text(
                text = elapsed,
                fontSize = 58.sp,
                fontWeight = FontWeight.Black,
                color = tokens.text,
                maxLines = 1,
                modifier = Modifier.semantics { contentDescription = "Elapsed time $elapsed" }
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Metric(
                    value = WorkoutFormatters.formatDistanceKm(tracking.distanceMeters),
                    label = "km",
                    tokens = tokens
                )
                Metric(
                    value = WorkoutFormatters.formatPace(tracking.currentPaceSecondsPerKm),
                    label = "min/km",
                    tokens = tokens
                )
                Metric(
                    value = tracking.estimatedCalories.toString(),
                    label = "kcal",
                    tokens = tokens
                )
            }
            if (state.displayedActivity == ActivityType.OUTDOOR_CYCLING) {
                Text(
                    text = "${WorkoutFormatters.formatSpeedKmh(tracking.currentSpeedKmh)} km/h",
                    style = MaterialTheme.typography.titleMedium,
                    color = tokens.text,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun Metric(value: String, label: String, tokens: com.calovelo.app.presentation.theme.CaloTokens) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 28.sp, fontWeight = FontWeight.Black, color = tokens.text, maxLines = 1)
        Text(label, style = MaterialTheme.typography.labelMedium, color = tokens.muted)
    }
}

@Composable
private fun ControlButtons(
    isTracking: Boolean,
    isPaused: Boolean,
    tokens: com.calovelo.app.presentation.theme.CaloTokens,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onFinish: () -> Unit
) {
    val buttonHeight = 64.dp
    val shape = RoundedCornerShape(22.dp)

    if (!isTracking) {
        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth().height(buttonHeight),
            shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = tokens.startFill,
                contentColor = tokens.onStart
            )
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(28.dp))
            Spacer(Modifier.size(8.dp))
            Text("Start Workout", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    } else {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (isPaused) {
                Button(
                    onClick = onResume,
                    modifier = Modifier.weight(1f).height(buttonHeight),
                    shape = shape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = tokens.startFill,
                        contentColor = tokens.onStart
                    )
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.size(6.dp))
                    Text("Resume", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                OutlinedButton(
                    onClick = onPause,
                    modifier = Modifier.weight(1f).height(buttonHeight),
                    shape = shape,
                    border = androidx.compose.foundation.BorderStroke(1.dp, tokens.border.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = tokens.text)
                ) {
                    Icon(Icons.Filled.Pause, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.size(6.dp))
                    Text("Pause", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            Button(
                onClick = onFinish,
                modifier = Modifier.weight(1f).height(buttonHeight),
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = tokens.proteinMark,
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Filled.Stop, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(Modifier.size(6.dp))
                Text("Finish", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/** Lightweight route preview drawn on a Canvas (no Maps SDK or API key required). */
@Composable
private fun RoutePreview(
    points: List<LatLng>,
    isTracking: Boolean,
    tokens: com.calovelo.app.presentation.theme.CaloTokens,
    modifier: Modifier = Modifier
) {
    val lineColor = tokens.mapRoute
    val startColor = tokens.mapBeacon
    val currentColor = tokens.proteinMark

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(tokens.mapBackground)
            .border(1.dp, tokens.border.copy(alpha = 0.35f), RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .semantics { contentDescription = "Route preview with ${points.size} recorded points" }
        ) {
            if (points.isEmpty()) return@Canvas
            val projected = projectRoute(points, size.width, size.height)

            if (projected.size >= 2) {
                val path = Path().apply {
                    moveTo(projected.first().x, projected.first().y)
                    for (i in 1 until projected.size) lineTo(projected[i].x, projected[i].y)
                }
                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
            drawCircle(color = startColor, radius = 8.dp.toPx(), center = projected.first())
            drawCircle(color = currentColor, radius = 10.dp.toPx(), center = projected.last())
        }
        if (points.isEmpty()) {
            Text(
                text = if (isTracking) {
                    "Waiting for a GPS fix…"
                } else {
                    "Live GPS route will appear here"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = tokens.muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

private const val MIN_SPAN_DEGREES = 1e-5

/**
 * Equirectangular projection fitted into a [width] x [height] box, keeping the aspect ratio
 * (longitude is scaled by cos(latitude)) and north pointing up.
 */
private fun projectRoute(points: List<LatLng>, width: Float, height: Float): List<Offset> {
    var minLat = Double.MAX_VALUE
    var maxLat = -Double.MAX_VALUE
    var minLng = Double.MAX_VALUE
    var maxLng = -Double.MAX_VALUE
    for (p in points) {
        minLat = min(minLat, p.latitude)
        maxLat = max(maxLat, p.latitude)
        minLng = min(minLng, p.longitude)
        maxLng = max(maxLng, p.longitude)
    }

    val lngScale = cos(Math.toRadians((minLat + maxLat) / 2.0))
    val spanX = max((maxLng - minLng) * lngScale, MIN_SPAN_DEGREES)
    val spanY = max(maxLat - minLat, MIN_SPAN_DEGREES)
    val scale = min(width / spanX, height / spanY)
    val offsetX = (width - spanX * scale) / 2.0
    val offsetY = (height - spanY * scale) / 2.0

    return points.map { p ->
        Offset(
            x = (offsetX + (p.longitude - minLng) * lngScale * scale).toFloat(),
            y = (offsetY + (maxLat - p.latitude) * scale).toFloat()
        )
    }
}

private fun requiredPermissions(): List<String> = buildList {
    add(Manifest.permission.ACCESS_FINE_LOCATION)
    add(Manifest.permission.ACCESS_COARSE_LOCATION)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) add(Manifest.permission.POST_NOTIFICATIONS)
}

private fun hasFineLocation(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED
