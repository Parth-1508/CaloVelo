package com.calovelo.app.presentation.gym

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calovelo.app.presentation.theme.CaloTheme
import com.calovelo.app.presentation.training.GymTrackerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GymTrackerContent(
    modifier: Modifier = Modifier,
    viewModel: GymTrackerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tokens = CaloTheme.tokens

    Scaffold(
        containerColor = tokens.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Today's Workout", color = tokens.text) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = tokens.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: Open Exercise Search Sheet */ },
                containerColor = tokens.startFill
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Exercise", tint = tokens.onStart)
            }
        }
    ) { padding ->
        if (uiState.session == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Button(
                    onClick = viewModel::onStartSession,
                    colors = ButtonDefaults.buttonColors(containerColor = tokens.startFill, contentColor = tokens.onStart)
                ) {
                    Text("Start Gym Session")
                }
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    Text("Volume: ${uiState.totalVolumeKg} kg", color = tokens.startFill, style = MaterialTheme.typography.titleLarge)
                }

                items(uiState.exercises) { exercise ->
                    Surface(
                        color = tokens.surface.copy(alpha = tokens.surfaceAlpha),
                        shape = MaterialTheme.shapes.extraLarge,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(exercise.name, color = tokens.text, fontWeight = FontWeight.Bold, fontSize = 18.sp)

                            exercise.sets.forEachIndexed { index, set ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .background(tokens.startFill, shape = MaterialTheme.shapes.small),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Filled.Check, contentDescription = null, tint = tokens.onStart, modifier = Modifier.size(16.dp))
                                        }
                                        Text("Set ${index + 1}", color = tokens.muted, fontSize = 14.sp)
                                    }

                                    Text("${set.weightKg} kg × ${set.reps}", color = tokens.text, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            Button(
                                onClick = { viewModel.onLogSet(exercise.exerciseId, 20.0, 10) },
                                colors = ButtonDefaults.buttonColors(containerColor = tokens.surface, contentColor = tokens.text),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("+ Log Set")
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = { viewModel.onFinishSession() },
                        colors = ButtonDefaults.buttonColors(containerColor = tokens.proteinMark, contentColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Finish Workout")
                    }
                }
            }
        }
    }
}