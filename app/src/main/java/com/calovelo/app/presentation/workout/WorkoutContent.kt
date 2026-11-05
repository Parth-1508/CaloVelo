package com.calovelo.app.presentation.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
    onStartWorkout: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val tokens = CaloTheme.tokens
    var isRecording by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var showRpeSheet by remember { mutableStateOf(false) }
    var rpeValue by remember { mutableStateOf(RpeScale.DEFAULT.toFloat()) }
    var selectedFeeling by remember { mutableStateOf<FeelingTag?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(tokens.background, tokens.surface.copy(alpha = 0.15f), tokens.background)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Header with Back ────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(tokens.surface.copy(alpha = 0.5f))
                        .border(1.dp, tokens.border.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = tokens.text,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "LIVE TRACKING",
                        style = MaterialTheme.typography.labelSmall,
                        color = tokens.energyMark,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        "Workout & Recovery",
                        style = MaterialTheme.typography.headlineMedium,
                        color = tokens.text,
                        fontWeight = FontWeight.Black
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(tokens.greenStateContainer ?: tokens.surface.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(if (isRecording) "RECORDING" else "READY", color = tokens.energyMark, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                }
            }

            // ── Live Stats Card ─────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(tokens.surface.copy(alpha = tokens.surfaceAlpha))
                    .border(1.dp, tokens.border.copy(alpha = 0.3f), RoundedCornerShape(28.dp))
                    .padding(24.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("00:42:18", style = MaterialTheme.typography.displayMedium, color = tokens.text, fontWeight = FontWeight.Black)
                    Text("Elapsed Time", style = MaterialTheme.typography.bodySmall, color = tokens.muted)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        WorkoutStatItem("Distance", "6.24 km", tokens)
                        WorkoutStatItem("Pace", "4:48 /km", tokens)
                        WorkoutStatItem("Speed", "12.5 km/h", tokens)
                    }

                    HorizontalDivider(color = tokens.border.copy(alpha = 0.2f))

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

            // ── Controls ────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!isRecording) {
                    Button(
                        onClick = { isRecording = true; isPaused = false; onStartWorkout() },
                        colors = ButtonDefaults.buttonColors(containerColor = tokens.startFill, contentColor = tokens.onStart),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Start Live GPS Tracking", fontWeight = FontWeight.Bold)
                    }
                } else {
                    OutlinedButton(
                        onClick = { isPaused = !isPaused },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = tokens.text),
                        border = androidx.compose.foundation.BorderStroke(1.dp, tokens.border.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f).height(52.dp)
                    ) {
                        Icon(if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (isPaused) "Resume" else "Pause", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            isRecording = false
                            isPaused = false
                            showRpeSheet = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = tokens.proteinMark, contentColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f).height(52.dp)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Finish", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // ── Recovery Card ───────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background((tokens.aiStateContainer ?: tokens.surface).copy(alpha = 0.6f))
                    .border(1.dp, tokens.border.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Favorite, contentDescription = null, tint = tokens.heartMark)
                        Text("Recovery & Strain Analysis", style = MaterialTheme.typography.titleMedium, color = tokens.text, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        "Your cardio strain is balanced. Recommended active recovery: 20 min easy spin or foam rolling.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = tokens.muted
                    )
                }
            }

            Spacer(Modifier.height(60.dp))
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
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(value, style = MaterialTheme.typography.titleMedium, color = tokens.text, fontWeight = FontWeight.Black)
        Text(label, style = MaterialTheme.typography.labelSmall, color = tokens.muted)
    }
}
