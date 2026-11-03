package com.calovelo.app.presentation.workout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calovelo.app.domain.model.FeelingTag
import com.calovelo.app.domain.model.RpeScale
import com.calovelo.app.presentation.theme.CaloTheme
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorkoutContent(
    modifier: Modifier = Modifier,
    onStartWorkout: () -> Unit = {}
) {
    val tokens = CaloTheme.tokens
    var isRecording by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var showRpeSheet by remember { mutableStateOf(false) }
    var rpeValue by remember { mutableStateOf(RpeScale.DEFAULT.toFloat()) }
    var selectedFeeling by remember { mutableStateOf<FeelingTag?>(null) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = tokens.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("ACTIVE TRACKING", style = MaterialTheme.typography.labelMedium, color = tokens.muted, fontWeight = FontWeight.Bold)
                    Text("Workout & Recovery", style = MaterialTheme.typography.headlineMedium, color = tokens.text, fontWeight = FontWeight.Bold)
                }
                Badge(containerColor = tokens.greenStateContainer ?: tokens.surface) {
                    Text(if (isRecording) "RECORDING" else "READY", color = tokens.energyMark, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                }
            }

            // Live Stats Card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = tokens.surface,
                border = BorderStroke(1.dp, tokens.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("00:42:18", style = MaterialTheme.typography.displayMedium, color = tokens.text, fontWeight = FontWeight.Bold)
                    Text("Elapsed Time", style = MaterialTheme.typography.bodySmall, color = tokens.muted)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        WorkoutStatItem("Distance", "6.24 km", tokens)
                        WorkoutStatItem("Pace", "4:48 /km", tokens)
                        WorkoutStatItem("Speed", "12.5 km/h", tokens)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        WorkoutStatItem("GPS Accuracy", "3.2m (High)", tokens)
                        WorkoutStatItem("Calories", "480 kcal", tokens)
                        WorkoutStatItem("Heart Rate", "156 bpm", tokens)
                    }
                }
            }

            // Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!isRecording) {
                    Button(
                        onClick = { isRecording = true; isPaused = false; onStartWorkout() },
                        colors = ButtonDefaults.buttonColors(containerColor = tokens.startFill, contentColor = tokens.onStart),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Start Tracking")
                    }
                } else {
                    OutlinedButton(
                        onClick = { isPaused = !isPaused },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = tokens.text),
                        border = BorderStroke(1.dp, tokens.border),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (isPaused) "Resume" else "Pause")
                    }

                    Button(
                        onClick = {
                            isRecording = false
                            isPaused = false
                            showRpeSheet = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = tokens.proteinMark, contentColor = Color.White),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Finish")
                    }
                }
            }

            // Recovery Session Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = tokens.surface,
                border = BorderStroke(1.dp, tokens.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Favorite, contentDescription = null, tint = tokens.heartMark)
                        Text("Recovery & Strain Analysis", style = MaterialTheme.typography.titleMedium, color = tokens.text, fontWeight = FontWeight.Bold)
                    }
                    Text("Your cardio load is balanced. Recommended active recovery: 20 min easy spin or stretching.", style = MaterialTheme.typography.bodyMedium, color = tokens.muted)
                }
            }
        }
    }

    if (showRpeSheet) {
        val score = rpeValue.roundToInt().coerceIn(RpeScale.MIN, RpeScale.MAX)
        AlertDialog(
            onDismissRequest = { showRpeSheet = false },
            title = { Text("Workout Feedback (RPE)", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(RpeScale.label(score), style = MaterialTheme.typography.titleMedium, color = tokens.energyMark, fontWeight = FontWeight.Bold)
                    Slider(
                        value = rpeValue,
                        onValueChange = { rpeValue = it },
                        valueRange = 1f..10f,
                        steps = 8
                    )
                    Text("How do you feel?", style = MaterialTheme.typography.titleSmall)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FeelingTag.entries.forEach { tag ->
                            FilterChip(
                                selected = selectedFeeling == tag,
                                onClick = { selectedFeeling = if (selectedFeeling == tag) null else tag },
                                label = { Text(tag.label) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showRpeSheet = false }) {
                    Text("Save Feedback")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRpeSheet = false }) {
                    Text("Skip")
                }
            },
            containerColor = tokens.surface
        )
    }
}

@Composable
fun WorkoutStatItem(label: String, value: String, tokens: com.calovelo.app.presentation.theme.CaloTokens) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = tokens.muted)
        Text(value, style = MaterialTheme.typography.titleMedium, color = tokens.text, fontWeight = FontWeight.Bold)
    }
}
