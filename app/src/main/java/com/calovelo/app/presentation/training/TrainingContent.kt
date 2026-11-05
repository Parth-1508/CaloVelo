package com.calovelo.app.presentation.training

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.calovelo.app.presentation.theme.CaloTheme

@Composable
fun TrainingContent(
    modifier: Modifier = Modifier,
    onStartWorkout: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val tokens = CaloTheme.tokens
    val daysOfWeek = listOf("Mon" to "14", "Tue" to "15", "Wed" to "16", "Thu" to "17", "Fri" to "18", "Sat" to "19", "Sun" to "20")
    var selectedDay by remember { mutableStateOf("Wed") }

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
            // ── Top Bar with Back ───────────────────
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
                        "TRAINING PROGRAM",
                        style = MaterialTheme.typography.labelSmall,
                        color = tokens.energyMark,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        "Schedule & Phases",
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
                    Text("Week 4 / 12", color = tokens.energyMark, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                }
            }

            // ── Weekly Calendar Strip ───────────────
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(daysOfWeek) { (day, date) ->
                    val isSelected = selectedDay == day
                    Box(
                        modifier = Modifier
                            .width(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isSelected) tokens.energyMark
                                else tokens.surface.copy(alpha = tokens.surfaceAlpha)
                            )
                            .border(
                                1.dp,
                                if (isSelected) tokens.energyMark else tokens.border.copy(alpha = 0.3f),
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { selectedDay = day }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                day,
                                color = if (isSelected) tokens.onStart else tokens.muted,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                date,
                                color = if (isSelected) tokens.onStart else tokens.text,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            // ── Today's Threshold Session Card ──────
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
                        Text(
                            "Today's Threshold Session",
                            style = MaterialTheme.typography.titleLarge,
                            color = tokens.text,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(tokens.energyMark.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Zone 4", color = tokens.energyMark, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TrainingTargetStat("Target Distance", "12.5 km", tokens)
                        TrainingTargetStat("Target Duration", "55:00 min", tokens)
                        TrainingTargetStat("Avg Pace", "4:45 /km", tokens)
                    }

                    Button(
                        onClick = onStartWorkout,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = tokens.startFill, contentColor = tokens.onStart),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Start Workout Session", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // ── Workout Phase Breakdown ─────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(tokens.surface.copy(alpha = tokens.surfaceAlpha))
                    .border(1.dp, tokens.border.copy(alpha = 0.3f), RoundedCornerShape(26.dp))
                    .padding(22.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "Workout Phase Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        color = tokens.text,
                        fontWeight = FontWeight.Bold
                    )

                    PhaseStepItem("1. Dynamic Warmup", "10 min easy jog & mobility drills", tokens.hydrationMark, tokens)
                    HorizontalDivider(color = tokens.border.copy(alpha = 0.2f))
                    PhaseStepItem("2. Tempo Threshold Interval", "35 min @ threshold pace (4:45 /km)", tokens.energyMark, tokens)
                    HorizontalDivider(color = tokens.border.copy(alpha = 0.2f))
                    PhaseStepItem("3. Recovery Cooldown", "10 min easy walk & static hamstring stretch", tokens.muted, tokens)
                }
            }

            // ── Recovery Session Card ───────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background((tokens.aiStateContainer ?: tokens.surface).copy(alpha = 0.6f))
                    .border(1.dp, tokens.border.copy(alpha = 0.3f), RoundedCornerShape(22.dp))
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(tokens.hydrationMark.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SelfImprovement, contentDescription = null, tint = tokens.hydrationMark, modifier = Modifier.size(22.dp))
                        }
                        Column {
                            Text("Post-Workout Recovery", style = MaterialTheme.typography.titleSmall, color = tokens.text, fontWeight = FontWeight.Bold)
                            Text("15 min lower body mobility session", style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                        }
                    }
                    Button(
                        onClick = {},
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = tokens.hydrationMark, contentColor = tokens.onStart)
                    ) {
                        Text("Start", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(60.dp))
        }
    }
}

@Composable
private fun TrainingTargetStat(label: String, value: String, tokens: com.calovelo.app.presentation.theme.CaloTokens) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = tokens.text, fontWeight = FontWeight.Black)
        Text(label, style = MaterialTheme.typography.labelSmall, color = tokens.muted)
    }
}

@Composable
private fun PhaseStepItem(title: String, description: String, indicatorColor: Color, tokens: com.calovelo.app.presentation.theme.CaloTokens) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(indicatorColor)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = tokens.text, fontWeight = FontWeight.Bold)
            Text(description, style = MaterialTheme.typography.bodySmall, color = tokens.muted)
        }
    }
}
