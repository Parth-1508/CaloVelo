package com.calovelo.app.presentation.goals

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
import com.calovelo.app.presentation.theme.CaloTheme

@Composable
fun GoalsContent(
    modifier: Modifier = Modifier,
    dailyCalorieGoal: Int = 2500,
    dailyWaterGoalMl: Int = 3000,
    weeklyDistanceGoalKm: Float = 50f,
    onSave: (calories: Int, waterMl: Int, distanceKm: Float) -> Unit = { _, _, _ -> },
    onNavigateBack: () -> Unit = {}
) {
    val tokens = CaloTheme.tokens

    var calorieSlider by remember(dailyCalorieGoal) { mutableFloatStateOf(dailyCalorieGoal.toFloat()) }
    var waterSlider by remember(dailyWaterGoalMl) { mutableFloatStateOf(dailyWaterGoalMl.toFloat()) }
    var distanceSlider by remember(weeklyDistanceGoalKm) { mutableFloatStateOf(weeklyDistanceGoalKm) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        tokens.background,
                        tokens.surface.copy(alpha = 0.15f),
                        tokens.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ── Header with back ────────────────────
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
                Column {
                    Text(
                        "PERSONAL TARGETS",
                        style = MaterialTheme.typography.labelSmall,
                        color = tokens.muted,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "Goals & Targets",
                        style = MaterialTheme.typography.headlineMedium,
                        color = tokens.text,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // ── Calorie Goal ────────────────────────
            GoalSliderCard(
                title = "Daily Calorie Goal",
                icon = Icons.Default.LocalFireDepartment,
                iconTint = tokens.energyMark,
                value = calorieSlider,
                onValueChange = { calorieSlider = it },
                valueRange = 1200f..5000f,
                steps = 37,
                displayValue = "${calorieSlider.toInt()} kcal",
                tokens = tokens
            )

            // ── Water Goal ──────────────────────────
            GoalSliderCard(
                title = "Daily Water Goal",
                icon = Icons.Default.WaterDrop,
                iconTint = tokens.hydrationMark,
                value = waterSlider,
                onValueChange = { waterSlider = it },
                valueRange = 1000f..5000f,
                steps = 7,
                displayValue = "${waterSlider.toInt()} ml",
                tokens = tokens
            )

            // ── Weekly Distance Goal ────────────────
            GoalSliderCard(
                title = "Weekly Distance Goal",
                icon = Icons.Default.DirectionsRun,
                iconTint = tokens.nutritionMark,
                value = distanceSlider,
                onValueChange = { distanceSlider = it },
                valueRange = 10f..200f,
                steps = 18,
                displayValue = "${String.format("%.0f", distanceSlider)} km",
                tokens = tokens
            )

            // ── Motivational Card ───────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background((tokens.aiStateContainer ?: tokens.surface).copy(alpha = 0.7f))
                    .border(1.dp, tokens.border.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = tokens.aiMark,
                        modifier = Modifier.size(28.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Stay Consistent",
                            style = MaterialTheme.typography.titleSmall,
                            color = tokens.text,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "You're 88% on track this week. Keep up the momentum!",
                            style = MaterialTheme.typography.bodySmall,
                            color = tokens.muted
                        )
                    }
                }
            }

            // ── Save Button ─────────────────────────
            Button(
                onClick = {
                    onSave(
                        calorieSlider.toInt(),
                        waterSlider.toInt(),
                        distanceSlider
                    )
                },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = tokens.startFill,
                    contentColor = tokens.onStart
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Save Goals", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun GoalSliderCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    displayValue: String,
    tokens: com.calovelo.app.presentation.theme.CaloTokens
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(tokens.surface.copy(alpha = tokens.surfaceAlpha))
            .border(1.dp, tokens.border.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        color = tokens.text,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    displayValue,
                    style = MaterialTheme.typography.titleMedium,
                    color = iconTint,
                    fontWeight = FontWeight.Bold
                )
            }
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                steps = steps,
                colors = SliderDefaults.colors(
                    thumbColor = iconTint,
                    activeTrackColor = iconTint,
                    inactiveTrackColor = tokens.border.copy(alpha = 0.4f)
                )
            )
        }
    }
}
