package com.calovelo.app.presentation.tracking

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.Offset
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
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(
    onNavigateBack: () -> Unit,
    viewModel: ActiveWorkoutViewModel = hiltViewModel()
) {
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
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Workout") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
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
                onSelected = viewModel::onActivitySelected
            )

            MetricsPanel(state = state)

            RoutePreview(
                points = state.tracking.currentRoutePoints,
                isTracking = isTracking,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .heightIn(min = 140.dp)
            )

            ControlButtons(
                isTracking = isTracking,
                isPaused = state.tracking.isPaused,
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
    onSelected: (ActivityType) -> Unit
) {
    val options = ActivityType.entries
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, type ->
            SegmentedButton(
                selected = type == selected,
                onClick = { onSelected(type) },
                enabled = enabled,
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size)
            ) {
                Text(type.selectorLabel, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun MetricsPanel(state: ActiveWorkoutUiState) {
    val tracking = state.tracking
    val elapsed = WorkoutFormatters.formatElapsed(tracking.elapsedTimeSeconds)
    val statusLabel = when {
        !tracking.isTracking -> "READY"
        tracking.isPaused -> "PAUSED"
        else -> "RECORDING"
    }

    // inverseSurface / inverseOnSurface give maximum contrast in both light and dark themes.
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.inverseSurface,
        contentColor = MaterialTheme.colorScheme.inverseOnSurface
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = statusLabel,
                style = MaterialTheme.typography.labelLarge,
                letterSpacing = 2.sp
            )
            Text(
                text = elapsed,
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                modifier = Modifier.semantics { contentDescription = "Elapsed time $elapsed" }
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Metric(
                    value = WorkoutFormatters.formatDistanceKm(tracking.distanceMeters),
                    label = "km"
                )
                Metric(
                    value = WorkoutFormatters.formatPace(tracking.currentPaceSecondsPerKm),
                    label = "min/km"
                )
                Metric(
                    value = tracking.estimatedCalories.toString(),
                    label = "kcal"
                )
            }
            if (state.displayedActivity == ActivityType.OUTDOOR_CYCLING) {
                Text(
                    text = "${WorkoutFormatters.formatSpeedKmh(tracking.currentSpeedKmh)} km/h",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun Metric(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 32.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun ControlButtons(
    isTracking: Boolean,
    isPaused: Boolean,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onFinish: () -> Unit
) {
    val buttonHeight = 72.dp
    val shape = RoundedCornerShape(36.dp)

    if (!isTracking) {
        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth().height(buttonHeight),
            shape = shape
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(Modifier.size(8.dp))
            Text("Start", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    } else {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (isPaused) {
                Button(
                    onClick = onResume,
                    modifier = Modifier.weight(1f).height(buttonHeight),
                    shape = shape
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.size(6.dp))
                    Text("Resume", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                FilledTonalButton(
                    onClick = onPause,
                    modifier = Modifier.weight(1f).height(buttonHeight),
                    shape = shape
                ) {
                    Icon(Icons.Filled.Pause, contentDescription = null, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.size(6.dp))
                    Text("Pause", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
            Button(
                onClick = onFinish,
                modifier = Modifier.weight(1f).height(buttonHeight),
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Icon(Icons.Filled.Stop, contentDescription = null, modifier = Modifier.size(28.dp))
                Spacer(Modifier.size(6.dp))
                Text("Finish", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/** Lightweight route preview drawn on a Canvas (no Maps SDK or API key required). */
@Composable
private fun RoutePreview(
    points: List<LatLng>,
    isTracking: Boolean,
    modifier: Modifier = Modifier
) {
    val lineColor = MaterialTheme.colorScheme.primary
    val startColor = MaterialTheme.colorScheme.tertiary
    val currentColor = MaterialTheme.colorScheme.error

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box(contentAlignment = Alignment.Center) {
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
                        "Your route will appear here"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
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
