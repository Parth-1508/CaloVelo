package com.calovelo.app.presentation.gym

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calovelo.app.domain.model.MuscleGroup
import com.calovelo.app.domain.model.PendingFeedback
import com.calovelo.app.domain.model.SetType
import com.calovelo.app.presentation.theme.CaloTheme
import com.calovelo.app.presentation.tracking.RpeFeedbackBottomSheet
import com.calovelo.app.presentation.training.GymExerciseUi
import com.calovelo.app.presentation.training.GymSetUi
import com.calovelo.app.presentation.training.GymTrackerUiState
import com.calovelo.app.presentation.training.GymTrackerViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymTrackerContent(
    modifier: Modifier = Modifier,
    viewModel: GymTrackerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tokens = CaloTheme.tokens

    var showSearchSheet by remember { mutableStateOf(false) }
    var finishPromptActive by remember { mutableStateOf(false) }
    var exerciseToDelete by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.onMessageShown()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(tokens.background, tokens.surface.copy(alpha = 0.15f), tokens.background)
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            floatingActionButton = {
                if (uiState.session != null) {
                    ExtendedFloatingActionButton(
                        onClick = { showSearchSheet = true },
                        containerColor = tokens.startFill,
                        contentColor = tokens.onStart,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.padding(bottom = 80.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Add Exercise", fontWeight = FontWeight.Bold)
                    }
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                if (uiState.session == null) {
                    EmptySessionView(
                        uiState = uiState,
                        tokens = tokens,
                        onStart = viewModel::onStartSession
                    )
                } else {
                    ActiveSessionView(
                        uiState = uiState,
                        tokens = tokens,
                        onAddExerciseClicked = { showSearchSheet = true },
                        onFinishClicked = { finishPromptActive = true },
                        onDiscardClicked = viewModel::onDiscardSession,
                        onLogSet = { exId, weight, reps, type -> viewModel.onLogSet(exId, weight, reps, type = type) },
                        onUpdateSet = { setId, weight, reps, type -> viewModel.onUpdateSet(setId, weight, reps, null, type) },
                        onDeleteSet = viewModel::onDeleteSet,
                        onDeleteExercisePrompt = { exId -> exerciseToDelete = exId },
                        onOpenGuide = viewModel::onOpenGuide
                    )
                }
            }
        }
    }

    // ── Search & Muscle Filter Bottom Sheet ─────────
    if (showSearchSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showSearchSheet = false
                viewModel.onSearchQueryChanged("")
                viewModel.onMuscleGroupSelected(null)
            },
            containerColor = tokens.surface,
            dragHandle = { BottomSheetDefaults.DragHandle(color = tokens.muted) }
        ) {
            ExerciseSearchContent(
                viewModel = viewModel,
                tokens = tokens,
                onDismiss = {
                    showSearchSheet = false
                    viewModel.onSearchQueryChanged("")
                    viewModel.onMuscleGroupSelected(null)
                },
                onOpenGuide = viewModel::onOpenGuide
            )
        }
    }

    // ── 3D Exercise Guide Sheet ─────────────────────
    uiState.guideExercise?.let { exercise ->
        ExerciseGuideBottomSheet(
            exercise = exercise,
            onDismiss = viewModel::onCloseGuide
        )
    }

    // ── Delete Exercise Confirmation Dialog ─────────
    exerciseToDelete?.let { exId ->
        val exName = uiState.exercises.firstOrNull { it.exerciseId == exId }?.name ?: "Exercise"
        AlertDialog(
            onDismissRequest = { exerciseToDelete = null },
            title = { Text("Remove $exName?", fontWeight = FontWeight.Bold) },
            text = { Text("This will remove $exName and all its logged sets from this workout.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onDeleteExercise(exId)
                        exerciseToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = tokens.proteinMark, contentColor = Color.White)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { exerciseToDelete = null }) {
                    Text("Cancel", color = tokens.muted)
                }
            },
            containerColor = tokens.surface
        )
    }

    // ── Finish RPE Feedback Sheet ───────────────────
    if (finishPromptActive && uiState.session != null) {
        RpeFeedbackBottomSheet(
            pending = PendingFeedback(
                sessionId = uiState.session!!.sessionId,
                activityType = com.calovelo.app.domain.model.ActivityType.RUNNING,
                durationSeconds = ((System.currentTimeMillis() - uiState.session!!.startTimeMillis) / 1000L).coerceAtLeast(0L),
                distanceMeters = 0.0
            ),
            isSaving = uiState.isBusy,
            onSave = { _, _, notes ->
                viewModel.onFinishSession(notes)
                finishPromptActive = false
            },
            onDismiss = { finishPromptActive = false }
        )
    }
}

// ────────────────────────────────────────────────────────────────────
// Empty Session & Past History View
// ────────────────────────────────────────────────────────────────────

@Composable
private fun EmptySessionView(
    uiState: GymTrackerUiState,
    tokens: com.calovelo.app.presentation.theme.CaloTokens,
    onStart: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "STRENGTH & GYM",
                        style = MaterialTheme.typography.labelSmall,
                        color = tokens.energyMark,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "Workout Tracker",
                        style = MaterialTheme.typography.headlineMedium,
                        color = tokens.text,
                        fontWeight = FontWeight.Black
                    )
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(tokens.surface.copy(alpha = 0.6f))
                        .border(1.dp, tokens.border.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = tokens.text, modifier = Modifier.size(24.dp))
                }
            }
        }

        item {
            // Hero Start Session Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(tokens.surface.copy(alpha = tokens.surfaceAlpha))
                    .border(1.dp, tokens.border.copy(alpha = 0.3f), RoundedCornerShape(32.dp))
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(tokens.startFill.copy(alpha = 0.12f))
                            .border(1.5.dp, tokens.startFill.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = tokens.startFill,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "Ready to Lift?",
                            style = MaterialTheme.typography.headlineSmall,
                            color = tokens.text,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            "Track sets, weights, reps & 1RM across 2,900+ exercises with 3D guide cues.",
                            textAlign = TextAlign.Center,
                            color = tokens.muted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Button(
                        onClick = onStart,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = tokens.startFill, contentColor = tokens.onStart),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Start Workout Session", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Recent Gym Sessions History
        if (uiState.history.isNotEmpty()) {
            item {
                Text(
                    "RECENT WORKOUTS",
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.muted,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }

            items(uiState.history) { session ->
                val dateStr = remember(session.startTimeMillis) {
                    DateTimeFormatter.ofPattern("EEE, MMM d · h:mm a")
                        .withZone(ZoneId.systemDefault())
                        .format(Instant.ofEpochMilli(session.startTimeMillis))
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(tokens.surface.copy(alpha = tokens.surfaceAlpha))
                        .border(1.dp, tokens.border.copy(alpha = 0.25f), RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(dateStr, style = MaterialTheme.typography.titleMedium, color = tokens.text, fontWeight = FontWeight.Bold)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(tokens.greenStateContainer ?: tokens.surface.copy(alpha = 0.5f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    "${session.volumeKg.roundToInt()} kg",
                                    color = tokens.energyMark,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${session.setCount} sets", style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                            Text("${session.durationMinutes} min", style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                            if (!session.notes.isNullOrBlank()) {
                                Text(session.notes, style = MaterialTheme.typography.bodySmall, color = tokens.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(80.dp))
        }
    }
}

// ────────────────────────────────────────────────────────────────────
// Active Workout View
// ────────────────────────────────────────────────────────────────────

@Composable
private fun ActiveSessionView(
    uiState: GymTrackerUiState,
    tokens: com.calovelo.app.presentation.theme.CaloTokens,
    onAddExerciseClicked: () -> Unit,
    onFinishClicked: () -> Unit,
    onDiscardClicked: () -> Unit,
    onLogSet: (exerciseId: String, weightKg: Double, reps: Int, type: SetType) -> Unit,
    onUpdateSet: (setId: String, weightKg: Double, reps: Int, type: SetType) -> Unit,
    onDeleteSet: (setId: String) -> Unit,
    onDeleteExercisePrompt: (exerciseId: String) -> Unit,
    onOpenGuide: (exerciseId: String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            // Live Session Volume HUD Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(tokens.surface.copy(alpha = tokens.surfaceAlpha))
                    .border(1.dp, tokens.border.copy(alpha = 0.3f), RoundedCornerShape(28.dp))
                    .padding(24.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "ACTIVE WORKOUT",
                                color = tokens.energyMark,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Text(
                                "${uiState.totalVolumeKg.roundToInt()} kg",
                                color = tokens.text,
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(tokens.greenStateContainer ?: tokens.surface.copy(alpha = 0.5f))
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                "${uiState.totalSets} sets",
                                color = tokens.energyMark,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = onFinishClicked,
                            colors = ButtonDefaults.buttonColors(containerColor = tokens.proteinMark, contentColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Finish", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onDiscardClicked,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = tokens.muted),
                            border = androidx.compose.foundation.BorderStroke(1.dp, tokens.border.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text("Discard", color = tokens.muted)
                        }
                    }
                }
            }
        }

        if (uiState.exercises.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(tokens.surface.copy(alpha = 0.4f))
                        .padding(36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = tokens.muted, modifier = Modifier.size(44.dp))
                        Text("No exercises added yet.", color = tokens.muted, fontWeight = FontWeight.SemiBold)
                        Button(
                            onClick = onAddExerciseClicked,
                            colors = ButtonDefaults.buttonColors(containerColor = tokens.startFill, contentColor = tokens.onStart)
                        ) {
                            Text("Browse Exercise Catalog")
                        }
                    }
                }
            }
        }

        // List of Exercise Cards
        items(uiState.exercises, key = { it.exerciseId }) { exercise ->
            ExerciseCard(
                exercise = exercise,
                tokens = tokens,
                onLogSet = onLogSet,
                onUpdateSet = onUpdateSet,
                onDeleteSet = onDeleteSet,
                onDeleteExercise = { onDeleteExercisePrompt(exercise.exerciseId) },
                onOpenGuide = { onOpenGuide(exercise.exerciseId) }
            )
        }

        item {
            Spacer(Modifier.height(100.dp))
        }
    }
}

// ────────────────────────────────────────────────────────────────────
// Exercise Card with Interactive Sets & Guides
// ────────────────────────────────────────────────────────────────────

@Composable
private fun ExerciseCard(
    exercise: GymExerciseUi,
    tokens: com.calovelo.app.presentation.theme.CaloTokens,
    onLogSet: (exerciseId: String, weightKg: Double, reps: Int, type: SetType) -> Unit,
    onUpdateSet: (setId: String, weightKg: Double, reps: Int, type: SetType) -> Unit,
    onDeleteSet: (setId: String) -> Unit,
    onDeleteExercise: () -> Unit,
    onOpenGuide: () -> Unit
) {
    // Local state for the "Add Set" row
    val lastSet = exercise.sets.lastOrNull()
    var inputWeight by remember(exercise.exerciseId, exercise.sets.size) {
        mutableStateOf(lastSet?.weightKg?.toString() ?: "20.0")
    }
    var inputReps by remember(exercise.exerciseId, exercise.sets.size) {
        mutableStateOf(lastSet?.reps?.toString() ?: "10")
    }
    var selectedType by remember { mutableStateOf(SetType.NORMAL) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(tokens.surface.copy(alpha = tokens.surfaceAlpha))
            .border(1.dp, tokens.border.copy(alpha = 0.3f), RoundedCornerShape(26.dp))
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Header: Exercise Title, Badges, and Action Icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        exercise.name,
                        color = tokens.text,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        "${exercise.muscleGroup.name.replace("_", " ")} • ${exercise.equipmentType.name.replace("_", " ")}",
                        color = tokens.muted,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Guide Button
                    IconButton(onClick = onOpenGuide) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "Exercise Guide",
                            tint = tokens.energyMark,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Delete Exercise Button
                    IconButton(onClick = onDeleteExercise) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Delete Exercise",
                            tint = tokens.proteinMark.copy(alpha = 0.8f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Sets Table Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("SET", color = tokens.muted, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(0.7f))
                Text("WEIGHT (KG)", color = tokens.muted, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, modifier = Modifier.weight(1.5f))
                Text("REPS", color = tokens.muted, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, modifier = Modifier.weight(1.2f))
                Text("ACTIONS", color = tokens.muted, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
            }

            HorizontalDivider(color = tokens.border.copy(alpha = 0.3f))

            // Logged Sets Rows
            exercise.sets.forEach { set ->
                SetRow(
                    set = set,
                    tokens = tokens,
                    onUpdate = { newWeight, newReps -> onUpdateSet(set.setId, newWeight, newReps, set.setType) },
                    onDelete = { onDeleteSet(set.setId) }
                )
            }

            // ── Inline "Add Set" Stepper Section ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(tokens.background.copy(alpha = 0.6f))
                    .border(1.dp, tokens.border.copy(alpha = 0.2f), RoundedCornerShape(18.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Add Next Set (#${exercise.sets.size + 1})",
                        style = MaterialTheme.typography.labelSmall,
                        color = tokens.energyMark,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Weight input
                        OutlinedTextField(
                            value = inputWeight,
                            onValueChange = { inputWeight = it },
                            label = { Text("Weight kg") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = tokens.energyMark,
                                unfocusedBorderColor = tokens.border.copy(alpha = 0.5f),
                                focusedTextColor = tokens.text,
                                unfocusedTextColor = tokens.text
                            )
                        )

                        // Reps input
                        OutlinedTextField(
                            value = inputReps,
                            onValueChange = { inputReps = it },
                            label = { Text("Reps") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = tokens.energyMark,
                                unfocusedBorderColor = tokens.border.copy(alpha = 0.5f),
                                focusedTextColor = tokens.text,
                                unfocusedTextColor = tokens.text
                            )
                        )
                    }

                    // Quick steppers row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("+2.5", "+5", "+10").forEach { step ->
                            OutlinedButton(
                                onClick = {
                                    val current = inputWeight.toDoubleOrNull() ?: 20.0
                                    val added = step.replace("+", "").toDoubleOrNull() ?: 2.5
                                    inputWeight = String.format("%.1f", current + added)
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, tokens.border.copy(alpha = 0.4f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(step, style = MaterialTheme.typography.labelSmall, color = tokens.muted)
                            }
                        }
                        listOf("-1", "+1").forEach { step ->
                            OutlinedButton(
                                onClick = {
                                    val current = inputReps.toIntOrNull() ?: 10
                                    val change = step.toIntOrNull() ?: 1
                                    inputReps = (current + change).coerceAtLeast(1).toString()
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, tokens.border.copy(alpha = 0.4f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(step, style = MaterialTheme.typography.labelSmall, color = tokens.muted)
                            }
                        }
                    }

                    Button(
                        onClick = {
                            val w = inputWeight.toDoubleOrNull() ?: 20.0
                            val r = inputReps.toIntOrNull() ?: 10
                            onLogSet(exercise.exerciseId, w, r, selectedType)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = tokens.energyMark, contentColor = tokens.onStart),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Log Set #${exercise.sets.size + 1}", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ────────────────────────────────────────────────────────────────────
// Interactive Set Row with In-Place Edit & Delete
// ────────────────────────────────────────────────────────────────────

@Composable
private fun SetRow(
    set: GymSetUi,
    tokens: com.calovelo.app.presentation.theme.CaloTokens,
    onUpdate: (weightKg: Double, reps: Int) -> Unit,
    onDelete: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var editWeight by remember(set.weightKg) { mutableStateOf(set.weightKg.toString()) }
    var editReps by remember(set.reps) { mutableStateOf(set.reps.toString()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Set index badge
        Surface(
            shape = CircleShape,
            color = tokens.border.copy(alpha = 0.4f),
            modifier = Modifier.weight(0.7f).wrapContentWidth(Alignment.Start)
        ) {
            Text(
                "${set.setIndex}",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                color = tokens.text,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Weight
        Text(
            "${set.weightKg} kg",
            color = tokens.text,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(1.5f)
                .clickable { isEditing = true }
        )

        // Reps
        Text(
            "${set.reps}",
            color = tokens.text,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(1.2f)
                .clickable { isEditing = true }
        )

        // Actions: Edit & Delete
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { isEditing = true }, modifier = Modifier.size(30.dp)) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = tokens.muted, modifier = Modifier.size(16.dp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Delete", tint = tokens.proteinMark.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
            }
        }
    }

    if (isEditing) {
        AlertDialog(
            onDismissRequest = { isEditing = false },
            title = { Text("Edit Set #${set.setIndex}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editWeight,
                        onValueChange = { editWeight = it },
                        label = { Text("Weight (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editReps,
                        onValueChange = { editReps = it },
                        label = { Text("Reps") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val w = editWeight.toDoubleOrNull() ?: set.weightKg
                        val r = editReps.toIntOrNull() ?: set.reps
                        onUpdate(w, r)
                        isEditing = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { isEditing = false }) {
                    Text("Cancel")
                }
            },
            containerColor = tokens.surface
        )
    }
}

// ────────────────────────────────────────────────────────────────────
// Search & Muscle Category Filter Sheet
// ────────────────────────────────────────────────────────────────────

@Composable
private fun ExerciseSearchContent(
    viewModel: GymTrackerViewModel,
    tokens: com.calovelo.app.presentation.theme.CaloTokens,
    onDismiss: () -> Unit,
    onOpenGuide: (exerciseId: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val muscleGroups = remember {
        listOf(null to "All") + MuscleGroup.entries.map { it to it.name.replace("_", " ").lowercase().replaceFirstChar { c -> c.uppercase() } }
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        // Search bar
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { viewModel.onSearchQueryChanged(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search 2,900+ exercises...", color = tokens.muted) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = tokens.muted) },
            trailingIcon = {
                if (uiState.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = tokens.muted)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = tokens.energyMark,
                unfocusedBorderColor = tokens.border.copy(alpha = 0.5f),
                focusedTextColor = tokens.text,
                unfocusedTextColor = tokens.text,
                cursorColor = tokens.energyMark
            ),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Spacer(Modifier.height(14.dp))

        // Muscle Group Filter Chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(muscleGroups) { (group, label) ->
                val isSelected = uiState.selectedMuscleGroup == group
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.onMuscleGroupSelected(group) },
                    label = { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = tokens.energyMark,
                        selectedLabelColor = tokens.onStart,
                        containerColor = tokens.surface.copy(alpha = 0.5f),
                        labelColor = tokens.text
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // Search Results List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            items(uiState.searchResults, key = { it.exerciseId }) { result ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.onAddExercise(result.exerciseId)
                            onDismiss()
                        }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            result.name,
                            color = tokens.text,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${result.muscleGroup.name.replace("_", " ")} • ${result.equipmentType.name.replace("_", " ")}",
                            color = tokens.muted,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { onOpenGuide(result.exerciseId) }) {
                            Icon(Icons.Default.Info, contentDescription = "Guide", tint = tokens.muted, modifier = Modifier.size(20.dp))
                        }
                        if (result.isInSession) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Added", tint = tokens.energyMark)
                        } else {
                            IconButton(onClick = {
                                viewModel.onAddExercise(result.exerciseId)
                                onDismiss()
                            }) {
                                Icon(Icons.Default.AddCircleOutline, contentDescription = "Add", tint = tokens.energyMark)
                            }
                        }
                    }
                }
                HorizontalDivider(color = tokens.border.copy(alpha = 0.25f))
            }

            if (uiState.searchResults.isEmpty() && !uiState.isSearching) {
                item {
                    Text(
                        "No exercises found.",
                        color = tokens.muted,
                        modifier = Modifier.padding(vertical = 32.dp).fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
