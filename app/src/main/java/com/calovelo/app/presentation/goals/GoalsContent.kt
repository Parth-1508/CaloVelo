package com.calovelo.app.presentation.goals

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
            // Header with back
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = tokens.text)
                }
                Column {
                    Text("PERSONAL TARGETS", style = MaterialTheme.typography.labelMedium, color = tokens.muted, fontWeight = FontWeight.Bold)
                    Text("Goals & Targets", style = MaterialTheme.typography.headlineMedium, color = tokens.text, fontWeight = FontWeight.Bold)
                }
            }

            // Calorie Goal Card
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

            // Water Goal Card
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

            // Weekly Distance Goal Card
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

            // Motivational Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = tokens.aiStateContainer ?: tokens.surface,
                border = BorderStroke(1.dp, tokens.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = tokens.aiMark, modifier = Modifier.size(32.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Stay Consistent", style = MaterialTheme.typography.titleSmall, color = tokens.text, fontWeight = FontWeight.Bold)
                        Text("You're 88% on track this week. Keep up the momentum!", style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                    }
                }
            }

            // Save Button
            Button(
                onClick = {
                    onSave(
                        calorieSlider.toInt(),
                        waterSlider.toInt(),
                        distanceSlider
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = tokens.startFill,
                    contentColor = tokens.onStart
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Save Goals")
            }
        }
    }
}

@Composable
private fun GoalSliderCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    displayValue: String,
    tokens: com.calovelo.app.presentation.theme.CaloTokens
) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(icon, contentDescription = null, tint = iconTint)
                    Text(title, style = MaterialTheme.typography.titleMedium, color = tokens.text, fontWeight = FontWeight.Bold)
                }
                Text(displayValue, style = MaterialTheme.typography.titleMedium, color = iconTint, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                steps = steps,
                colors = SliderDefaults.colors(
                    thumbColor = iconTint,
                    activeTrackColor = iconTint,
                    inactiveTrackColor = tokens.border
                )
            )
        }
    }
}
