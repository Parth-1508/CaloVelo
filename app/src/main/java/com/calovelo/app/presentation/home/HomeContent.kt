package com.calovelo.app.presentation.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calovelo.app.presentation.theme.CaloTheme
import com.calovelo.app.presentation.theme.CaloThemeId

@Composable
fun HomeContent(
    modifier: Modifier = Modifier,
    userName: String = "Athlete",
    dailyCalorieGoal: Int = 2500,
    dailyWaterGoalMl: Int = 3000,
    waterIntakeMl: Int = 0,
    onStartWorkout: () -> Unit = {},
    onOpenScanner: () -> Unit = {}
) {
    val tokens = CaloTheme.tokens
    val scrollState = rememberScrollState()

    // Assuming we have dummy values for now until fully wired
    val consumedKcal = 1450
    val burnedKcal = 650
    val netKcal = consumedKcal - burnedKcal
    
    val caloriesProgress = if (dailyCalorieGoal > 0) (consumedKcal.toFloat() / dailyCalorieGoal).coerceIn(0f, 1f) else 0f
    val hydrationProgress = if (dailyWaterGoalMl > 0) (waterIntakeMl.toFloat() / dailyWaterGoalMl).coerceIn(0f, 1f) else 0f

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        tokens.background,
                        tokens.surface.copy(alpha = 0.2f),
                        tokens.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Premium Glassmorphic Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TODAY's OVERVIEW",
                        style = MaterialTheme.typography.labelSmall,
                        color = tokens.energyMark,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Hello, $userName",
                        style = MaterialTheme.typography.headlineMedium,
                        color = tokens.text,
                        fontWeight = FontWeight.Black,
                        fontSize = 32.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(tokens.surface.copy(alpha = 0.6f))
                        .border(1.dp, tokens.border.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = "Profile", tint = tokens.text, modifier = Modifier.size(28.dp))
                }
            }

            if (tokens.themeId == CaloThemeId.ThermalDark) {
                ThermalHomeVariant(onStartWorkout = onStartWorkout, onOpenScanner = onOpenScanner)
            } else {
                EmeraldHomeVariant(
                    caloriesProgress = caloriesProgress,
                    netCalories = "$netKcal / $dailyCalorieGoal kcal",
                    hydrationProgress = hydrationProgress,
                    hydrationText = "${waterIntakeMl / 1000f} / ${dailyWaterGoalMl / 1000f} L",
                    onStartWorkout = onStartWorkout, 
                    onOpenScanner = onOpenScanner
                )
            }
            
            Spacer(Modifier.height(80.dp)) // padding for bottom nav
        }
    }
}

@Composable
fun EmeraldHomeVariant(
    caloriesProgress: Float,
    netCalories: String,
    hydrationProgress: Float,
    hydrationText: String,
    onStartWorkout: () -> Unit,
    onOpenScanner: () -> Unit
) {
    val tokens = CaloTheme.tokens

    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        // Flat, elegant dashboard card instead of giant glass bubble
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = tokens.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Today's Energy", style = MaterialTheme.typography.titleMedium, color = tokens.text, fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .background((tokens.greenStateContainer ?: tokens.surface.copy(alpha = 0.5f)), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Active", color = tokens.energyMark, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }

                // Tight layout: Gauge on left, macros stacked on right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DualProgressGauge(
                        caloriesProgress = caloriesProgress,
                        netCalories = netCalories,
                        hydrationProgress = hydrationProgress,
                        hydrationText = hydrationText
                    )
                    
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        MiniMacroBar("Protein", "124g", tokens.proteinMark, tokens)
                        MiniMacroBar("Carbs", "210g", tokens.carbsMark, tokens)
                        MiniMacroBar("Fat", "68g", tokens.fatMark, tokens)
                    }
                }
            }
        }

        // Quick Actions (Compact, unified row)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionPill(
                icon = Icons.Default.DirectionsRun,
                text = "Run",
                color = tokens.startFill,
                textColor = tokens.onStart,
                onClick = onStartWorkout,
                modifier = Modifier.weight(1f)
            )
            QuickActionPill(
                icon = Icons.Default.Restaurant,
                text = "Scan",
                color = tokens.surface,
                textColor = tokens.text,
                onClick = onOpenScanner,
                modifier = Modifier.weight(1f)
            )
            QuickActionPill(
                icon = Icons.Default.WaterDrop,
                text = "Water",
                color = tokens.surface,
                textColor = tokens.text,
                onClick = {}, // TODO wire
                modifier = Modifier.weight(1f)
            )
        }

        // Recent Activity 
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("RECENT ACTIVITY", style = MaterialTheme.typography.labelSmall, color = tokens.muted, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = tokens.surface,
                modifier = Modifier.fillMaxWidth().clickable(onClick = onStartWorkout)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(tokens.energyMark.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.DirectionsRun, contentDescription = null, tint = tokens.energyMark, modifier = Modifier.size(20.dp))
                            }
                            Column {
                                Text("Morning Tempo Run", style = MaterialTheme.typography.titleMedium, color = tokens.text, fontWeight = FontWeight.Bold)
                                Text("Today, 7:30 AM", style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                            }
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = tokens.muted)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricMiniStat("Distance", "8.42 km", tokens)
                        MetricMiniStat("Duration", "42:15", tokens)
                        MetricMiniStat("Pace", "5:01 /km", tokens)
                    }
                }
            }
        }
    }
}

@Composable
fun ThermalHomeVariant(
    onStartWorkout: () -> Unit,
    onOpenScanner: () -> Unit
) {
    val tokens = CaloTheme.tokens

    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        // Hero Concentric Engine
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(tokens.surface.copy(alpha = 0.6f))
                .border(1.dp, tokens.border.copy(alpha = 0.3f), RoundedCornerShape(32.dp))
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp)) {
                Text(
                    "DAILY THERMAL LOAD",
                    style = MaterialTheme.typography.labelMedium,
                    color = tokens.energyMark,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )

                Box(
                    modifier = Modifier.size(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val progressAnim by animateFloatAsState(targetValue = 0.88f, animationSpec = tween(1200))
                    val stepsAnim by animateFloatAsState(targetValue = 0.65f, animationSpec = tween(1200))

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 18.dp.toPx()
                        drawArc(tokens.border, 0f, 360f, false, style = Stroke(strokeWidth, cap = StrokeCap.Round))
                        drawArc(
                            brush = Brush.sweepGradient(listOf(tokens.energyMark, tokens.nutritionMark)),
                            startAngle = -90f, sweepAngle = 360f * progressAnim, useCenter = false,
                            style = Stroke(strokeWidth, cap = StrokeCap.Round)
                        )
                        drawArc(tokens.border, 0f, 360f, false, style = Stroke(strokeWidth * 0.7f, cap = StrokeCap.Round), topLeft = Offset(strokeWidth * 0.8f, strokeWidth * 0.8f), size = Size(size.width - strokeWidth * 1.6f, size.height - strokeWidth * 1.6f))
                        drawArc(
                            color = tokens.hydrationMark, startAngle = -90f, sweepAngle = 360f * stepsAnim, useCenter = false,
                            style = Stroke(strokeWidth * 0.7f, cap = StrokeCap.Round), topLeft = Offset(strokeWidth * 0.8f, strokeWidth * 0.8f), size = Size(size.width - strokeWidth * 1.6f, size.height - strokeWidth * 1.6f)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("2,480", style = MaterialTheme.typography.displaySmall, color = tokens.text, fontWeight = FontWeight.Black)
                        Text("Kcal Burned", style = MaterialTheme.typography.labelMedium, color = tokens.muted, letterSpacing = 1.sp)
                    }
                }
            }
        }

        // Glassmorphic Action Blocks
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            GlassCard(modifier = Modifier.weight(1f).clickable { onOpenScanner() }, tokens) {
                Icon(Icons.Default.RestaurantMenu, contentDescription = null, tint = tokens.nutritionMark, modifier = Modifier.size(32.dp))
                Spacer(Modifier.height(12.dp))
                Text("Scan Meal", style = MaterialTheme.typography.titleMedium, color = tokens.text, fontWeight = FontWeight.Bold)
                Text("Analyze macros", style = MaterialTheme.typography.bodySmall, color = tokens.muted)
            }
            GlassCard(modifier = Modifier.weight(1f).clickable { onStartWorkout() }, tokens) {
                Icon(Icons.Default.FlashOn, contentDescription = null, tint = tokens.energyMark, modifier = Modifier.size(32.dp))
                Spacer(Modifier.height(12.dp))
                Text("Train", style = MaterialTheme.typography.titleMedium, color = tokens.text, fontWeight = FontWeight.Bold)
                Text("Start activity", style = MaterialTheme.typography.bodySmall, color = tokens.muted)
            }
        }
    }
}

@Composable
fun GlassCard(modifier: Modifier = Modifier, tokens: com.calovelo.app.presentation.theme.CaloTokens, content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(tokens.surface.copy(alpha = 0.5f))
            .border(1.dp, tokens.border.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Column(content = content)
    }
}

@Composable
fun QuickActionPill(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, color: Color, textColor: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color)
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = textColor, modifier = Modifier.size(20.dp))
            Text(text, color = textColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun DualProgressGauge(
    caloriesProgress: Float,
    netCalories: String,
    hydrationProgress: Float,
    hydrationText: String
) {
    val tokens = CaloTheme.tokens
    Box(
        modifier = Modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        val calAnim by animateFloatAsState(targetValue = caloriesProgress, animationSpec = tween(1200))
        val hydAnim by animateFloatAsState(targetValue = hydrationProgress, animationSpec = tween(1200))

        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 12.dp.toPx()
            val inset = stroke * 1.5f
            
            // Subtle track backgrounds
            drawArc(tokens.border.copy(alpha = 0.3f), 0f, 360f, false, style = Stroke(stroke, cap = StrokeCap.Round))
            drawArc(tokens.border.copy(alpha = 0.3f), 0f, 360f, false, style = Stroke(stroke * 0.7f, cap = StrokeCap.Round), topLeft = Offset(inset, inset), size = Size(size.width - inset * 2, size.height - inset * 2))

            // Progress arcs
            drawArc(tokens.energyMark, -90f, 360f * calAnim, false, style = Stroke(stroke, cap = StrokeCap.Round))
            drawArc(tokens.hydrationMark, -90f, 360f * hydAnim, false, style = Stroke(stroke * 0.7f, cap = StrokeCap.Round), topLeft = Offset(inset, inset), size = Size(size.width - inset * 2, size.height - inset * 2))
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = tokens.energyMark, modifier = Modifier.size(24.dp))
            Text(netCalories, style = MaterialTheme.typography.titleMedium, color = tokens.text, fontWeight = FontWeight.Black)
            Text(hydrationText, style = MaterialTheme.typography.labelMedium, color = tokens.hydrationMark, fontWeight = FontWeight.SemiBold)
        }
    }
}

// Removed unused MacroPillRow

@Composable
fun MiniMacroBar(label: String, value: String, accent: Color, tokens: com.calovelo.app.presentation.theme.CaloTokens) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(accent))
            Text(label, style = MaterialTheme.typography.labelSmall, color = tokens.muted)
        }
        Text(value, style = MaterialTheme.typography.titleSmall, color = tokens.text, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun MetricMiniStat(label: String, value: String, tokens: com.calovelo.app.presentation.theme.CaloTokens) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, color = tokens.text, fontWeight = FontWeight.Black)
        Text(label, style = MaterialTheme.typography.labelSmall, color = tokens.muted, letterSpacing = 0.5.sp)
    }
}
