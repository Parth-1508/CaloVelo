package com.calovelo.app.presentation.training

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
import com.calovelo.app.presentation.theme.CaloTheme

@Composable
fun TrainingContent(
    modifier: Modifier = Modifier
) {
    val tokens = CaloTheme.tokens

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
                    Text("TRAINING PROGRAM", style = MaterialTheme.typography.labelMedium, color = tokens.muted, fontWeight = FontWeight.Bold)
                    Text("Schedule & Phases", style = MaterialTheme.typography.headlineMedium, color = tokens.text, fontWeight = FontWeight.Bold)
                }
                Badge(containerColor = tokens.greenStateContainer ?: tokens.surface) {
                    Text("Week 4 / 12", color = tokens.energyMark, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                }
            }

            // Target Duration / Distance Card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = tokens.surface,
                border = BorderStroke(1.dp, tokens.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Today's Threshold Session", style = MaterialTheme.typography.titleMedium, color = tokens.text, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TrainingTargetStat("Target Distance", "12.5 km", tokens)
                        TrainingTargetStat("Target Duration", "55:00 min", tokens)
                        TrainingTargetStat("Intensity", "Zone 4", tokens)
                    }
                }
            }

            // Phase Breakdown
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = tokens.surface,
                border = BorderStroke(1.dp, tokens.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Workout Phase Breakdown", style = MaterialTheme.typography.titleMedium, color = tokens.text, fontWeight = FontWeight.Bold)

                    PhaseStepItem("1. Warmup", "10 min easy jog & dynamic drills", tokens.hydrationMark, tokens)
                    PhaseStepItem("2. Tempo Interval", "35 min @ threshold pace (4:45 /km)", tokens.energyMark, tokens)
                    PhaseStepItem("3. Cooldown", "10 min easy walk & static stretch", tokens.muted, tokens)
                }
            }

            // Recovery Session Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = tokens.surface,
                border = BorderStroke(1.dp, tokens.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.SelfImprovement, contentDescription = null, tint = tokens.hydrationMark)
                        Column {
                            Text("Post-Workout Recovery", style = MaterialTheme.typography.titleSmall, color = tokens.text, fontWeight = FontWeight.Bold)
                            Text("15 min lower body mobility session scheduled.", style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                        }
                    }
                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(containerColor = tokens.hydrationMark, contentColor = tokens.onStart)
                    ) {
                        Text("Start")
                    }
                }
            }
        }
    }
}

@Composable
fun TrainingTargetStat(label: String, value: String, tokens: com.calovelo.app.presentation.theme.CaloTokens) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = tokens.muted)
        Text(value, style = MaterialTheme.typography.bodyLarge, color = tokens.text, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PhaseStepItem(title: String, description: String, indicatorColor: Color, tokens: com.calovelo.app.presentation.theme.CaloTokens) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(indicatorColor)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = tokens.text, fontWeight = FontWeight.Bold)
            Text(description, style = MaterialTheme.typography.bodySmall, color = tokens.muted)
        }
    }
}
