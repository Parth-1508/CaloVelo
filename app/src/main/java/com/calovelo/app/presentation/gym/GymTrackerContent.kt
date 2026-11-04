package com.calovelo.app.presentation.gym

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calovelo.app.presentation.theme.CaloTheme
import com.calovelo.app.presentation.training.GymTrackerViewModel
import com.calovelo.app.presentation.training.GymExerciseUi
import com.calovelo.app.presentation.tracking.RpeFeedbackBottomSheet
import com.calovelo.app.domain.model.RpeScale

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

    Scaffold(
        containerColor = tokens.background,
        floatingActionButton = {
            if (uiState.session != null) {
                FloatingActionButton(
                    onClick = { showSearchSheet = true },
                    containerColor = tokens.startFill,
                    shape = CircleShape,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Exercise", tint = tokens.onStart)
                }
            }
        }
    ) { padding ->
        Box(modifier = modifier.fillMaxSize().padding(padding)) {
            if (uiState.session == null) {
                EmptySessionView(tokens, viewModel::onStartSession)
            } else {
                ActiveSessionView(
                    uiState = uiState,
                    tokens = tokens,
                    onFinishClicked = { finishPromptActive = true },
                    onLogSet = { exId -> viewModel.onLogSet(exId, 20.0, 10) }
                )
            }
        }
    }

    if (showSearchSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSearchSheet = false },
            containerColor = tokens.surface,
            dragHandle = { BottomSheetDefaults.DragHandle(color = tokens.muted) }
        ) {
            ExerciseSearchContent(viewModel, tokens, onDismiss = { showSearchSheet = false })
        }
    }

    if (finishPromptActive) {
        RpeFeedbackBottomSheet(
            pending = com.calovelo.app.domain.model.PendingFeedback(uiState.session!!.sessionId, com.calovelo.app.domain.model.ActivityType.RUNNING, 0L, 0.0),
            isSaving = uiState.isBusy,
            onSave = { _, _, notes ->
                viewModel.onFinishSession(notes)
                finishPromptActive = false
            },
            onDismiss = { finishPromptActive = false }
        )
    }
}

@Composable
private fun EmptySessionView(tokens: com.calovelo.app.presentation.theme.CaloTokens, onStart: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = tokens.startFill.copy(alpha = 0.1f),
            modifier = Modifier.size(120.dp)
        ) {
            Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = tokens.startFill, modifier = Modifier.padding(32.dp))
        }
        Spacer(Modifier.height(32.dp))
        Text("Ready to Lift?", style = MaterialTheme.typography.headlineMedium, color = tokens.text, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text("Start an empty session and build your workout as you go.", textAlign = TextAlign.Center, color = tokens.muted)
        Spacer(Modifier.height(48.dp))
        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = tokens.startFill, contentColor = tokens.onStart)
        ) {
            Text("Start Empty Workout", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ActiveSessionView(
    uiState: com.calovelo.app.presentation.training.GymTrackerUiState,
    tokens: com.calovelo.app.presentation.theme.CaloTokens,
    onFinishClicked: () -> Unit,
    onLogSet: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 24.dp, bottom = 120.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("ACTIVE WORKOUT", color = tokens.energyMark, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Text("Session Volume", color = tokens.text, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                }
                Text("${uiState.totalVolumeKg.toInt()} kg", color = tokens.text, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            }
        }

        if (uiState.exercises.isEmpty()) {
            item {
                Text("Tap + to add your first exercise.", color = tokens.muted, modifier = Modifier.padding(top = 40.dp))
            }
        }

        items(uiState.exercises) { exercise ->
            ExerciseCard(exercise, tokens, onLogSet)
        }

        if (uiState.exercises.isNotEmpty()) {
            item {
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onFinishClicked,
                    colors = ButtonDefaults.buttonColors(containerColor = tokens.proteinMark, contentColor = Color.White),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("Finish Workout", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ExerciseCard(
    exercise: GymExerciseUi,
    tokens: com.calovelo.app.presentation.theme.CaloTokens,
    onLogSet: (String) -> Unit
) {
    Surface(
        color = tokens.surface.copy(alpha = tokens.surfaceAlpha),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(exercise.name, color = tokens.text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Icon(Icons.Default.MoreHoriz, contentDescription = "Options", tint = tokens.muted)
            }
            Text(exercise.muscleGroup.name.replace("_", " "), color = tokens.muted, style = MaterialTheme.typography.labelMedium)
            
            Spacer(Modifier.height(24.dp))
            
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("SET", color = tokens.muted, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(0.5f))
                Text("KG", color = tokens.muted, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                Text("REPS", color = tokens.muted, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                Box(modifier = Modifier.weight(0.5f)) // Spacer for checkmark
            }

            exercise.sets.forEachIndexed { index, set ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(shape = CircleShape, color = tokens.border, modifier = Modifier.weight(0.5f).wrapContentWidth(Alignment.Start)) {
                        Text("${index + 1}", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), color = tokens.text, fontSize = 12.sp)
                    }
                    Text("${set.weightKg}", color = tokens.text, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                    Text("${set.reps}", color = tokens.text, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                    
                    Surface(shape = RoundedCornerShape(8.dp), color = tokens.energyMark, modifier = Modifier.weight(0.5f).wrapContentWidth(Alignment.End)) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = tokens.onStart, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp).size(16.dp))
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            TextButton(
                onClick = { onLogSet(exercise.exerciseId) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = tokens.energyMark)
                Spacer(Modifier.width(8.dp))
                Text("Add Set", color = tokens.energyMark, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ExerciseSearchContent(
    viewModel: GymTrackerViewModel,
    tokens: com.calovelo.app.presentation.theme.CaloTokens,
    onDismiss: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { viewModel.onSearchQueryChanged(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search 2,900+ exercises...", color = tokens.muted) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = tokens.muted) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = tokens.energyMark,
                unfocusedBorderColor = tokens.border,
                focusedTextColor = tokens.text,
                unfocusedTextColor = tokens.text,
                cursorColor = tokens.energyMark
            ),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )
        
        Spacer(Modifier.height(16.dp))
        
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
            items(uiState.searchResults) { result ->
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { 
                        viewModel.onAddExercise(result.exerciseId)
                        onDismiss()
                    }.padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(result.name, color = tokens.text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(result.muscleGroup.name.replace("_", " ") + " • " + result.equipmentType.name.replace("_", " "), color = tokens.muted, style = MaterialTheme.typography.bodySmall)
                    }
                    if (result.isInSession) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Added", tint = tokens.energyMark)
                    } else {
                        Icon(Icons.Default.AddCircleOutline, contentDescription = "Add", tint = tokens.muted)
                    }
                }
                HorizontalDivider(color = tokens.border.copy(alpha = 0.5f))
            }
            if (uiState.searchResults.isEmpty() && uiState.searchQuery.isNotEmpty()) {
                item {
                    Text("No exercises found.", color = tokens.muted, modifier = Modifier.padding(vertical = 32.dp).fillMaxWidth(), textAlign = TextAlign.Center)
                }
            }
        }
    }
}