package com.calovelo.app.presentation.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
    onStartWorkout: () -> Unit = {},
    onOpenScanner: () -> Unit = {}
) {
    val tokens = CaloTheme.tokens
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = tokens.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header / Greeting
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (tokens.themeId == CaloThemeId.ThermalDark) "THERMAL ENGINE" else "CALOVELO ACTIVE",
                        style = MaterialTheme.typography.labelMedium,
                        color = tokens.muted,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Hello, Athlete",
                        style = MaterialTheme.typography.headlineMedium,
                        color = tokens.text,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = tokens.surface,
                    border = BorderStroke(1.dp, tokens.border),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = tokens.text
                        )
                    }
                }
            }

            if (tokens.themeId == CaloThemeId.ThermalDark) {
                ThermalHomeVariant(onStartWorkout = onStartWorkout, onOpenScanner = onOpenScanner)
            } else {
                EmeraldHomeVariant(onStartWorkout = onStartWorkout, onOpenScanner = onOpenScanner)
            }
        }
    }
}

@Composable
fun EmeraldHomeVariant(
    onStartWorkout: () -> Unit,
    onOpenScanner: () -> Unit
) {
    val tokens = CaloTheme.tokens

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Dual Progress Gauge Card (Net Calories & Hydration)
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Daily Energy & Hydration",
                        style = MaterialTheme.typography.titleMedium,
                        color = tokens.text,
                        fontWeight = FontWeight.Bold
                    )
                    Badge(containerColor = tokens.greenStateContainer ?: tokens.surface) {
                        Text("Active", color = tokens.energyMark, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DualProgressGauge(
                        caloriesProgress = 0.74f,
                        netCalories = "1,850 / 2,500 kcal",
                        hydrationProgress = 0.85f,
                        hydrationText = "2.55 / 3.0 L"
                    )
                }

                MacroPillRow()
            }
        }

        // Week Strip
        WeekStrip()

        // Recent Run Card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = tokens.surface,
            border = BorderStroke(1.dp, tokens.border),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DirectionsRun, contentDescription = null, tint = tokens.energyMark)
                        Text("Morning Tempo Run", style = MaterialTheme.typography.titleMedium, color = tokens.text, fontWeight = FontWeight.SemiBold)
                    }
                    Text("Today, 7:30 AM", style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricMiniStat("Distance", "8.42 km", tokens)
                    MetricMiniStat("Duration", "42:15", tokens)
                    MetricMiniStat("Pace", "5:01 /km", tokens)
                    MetricMiniStat("Burn", "610 kcal", tokens)
                }

                Button(
                    onClick = onStartWorkout,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = tokens.startFill,
                        contentColor = tokens.onStart
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Start New Workout")
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

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Concentric Energy/Steps Gauge Card
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
                Text(
                    "THERMAL OUTPUT & RECOVERY",
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.energyMark,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier.size(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val progressAnim by animateFloatAsState(targetValue = 0.88f, animationSpec = tween(1200))
                    val stepsAnim by animateFloatAsState(targetValue = 0.65f, animationSpec = tween(1200))

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 16.dp.toPx()

                        // Outer ring (Energy)
                        drawArc(
                            color = tokens.border,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(strokeWidth, cap = StrokeCap.Round)
                        )
                        drawArc(
                            brush = Brush.sweepGradient(listOf(tokens.energyMark, tokens.nutritionMark)),
                            startAngle = -90f,
                            sweepAngle = 360f * progressAnim,
                            useCenter = false,
                            style = Stroke(strokeWidth, cap = StrokeCap.Round)
                        )

                        // Inner ring (Steps / Load)
                        drawArc(
                            color = tokens.border,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(strokeWidth * 0.75f, cap = StrokeCap.Round),
                            topLeft = Offset(strokeWidth * 0.75f, strokeWidth * 0.75f),
                            size = Size(size.width - strokeWidth * 1.5f, size.height - strokeWidth * 1.5f)
                        )
                        drawArc(
                            color = tokens.hydrationMark,
                            startAngle = -90f,
                            sweepAngle = 360f * stepsAnim,
                            useCenter = false,
                            style = Stroke(strokeWidth * 0.75f, cap = StrokeCap.Round),
                            topLeft = Offset(strokeWidth * 0.75f, strokeWidth * 0.75f),
                            size = Size(size.width - strokeWidth * 1.5f, size.height - strokeWidth * 1.5f)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("2,480", style = MaterialTheme.typography.headlineLarge, color = tokens.text, fontWeight = FontWeight.Bold)
                        Text("Kcal Burned", style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("12,450", style = MaterialTheme.typography.titleMedium, color = tokens.text, fontWeight = FontWeight.Bold)
                        Text("Steps", style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("94%", style = MaterialTheme.typography.titleMedium, color = tokens.energyMark, fontWeight = FontWeight.Bold)
                        Text("Recovery", style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                    }
                }
            }
        }

        // Two Bento Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = tokens.surface,
                border = BorderStroke(1.dp, tokens.border),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = tokens.nutritionMark)
                    Text("AI Meal Scan", style = MaterialTheme.typography.titleSmall, color = tokens.text, fontWeight = FontWeight.Bold)
                    Text("Analyze macro breakdown instantly.", style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                    TextButton(onClick = onOpenScanner) {
                        Text("Scan Now", color = tokens.nutritionMark)
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = tokens.surface,
                border = BorderStroke(1.dp, tokens.border),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.FlashOn, contentDescription = null, tint = tokens.energyMark)
                    Text("Power Surge", style = MaterialTheme.typography.titleSmall, color = tokens.text, fontWeight = FontWeight.Bold)
                    Text("Threshold intervals active today.", style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                    TextButton(onClick = onStartWorkout) {
                        Text("Start Session", color = tokens.energyMark)
                    }
                }
            }
        }

        // Crew Invitation Card
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
                    Surface(
                        shape = CircleShape,
                        color = tokens.aiStateContainer ?: tokens.surface,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Group, contentDescription = null, tint = tokens.aiMark)
                        }
                    }
                    Column {
                        Text("Sunset Striders Invitation", style = MaterialTheme.typography.titleSmall, color = tokens.text, fontWeight = FontWeight.Bold)
                        Text("12 crew members riding at 6:00 PM", style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                    }
                }
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = tokens.aiMark, contentColor = tokens.aiInk)
                ) {
                    Text("Join")
                }
            }
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
        modifier = Modifier.size(160.dp),
        contentAlignment = Alignment.Center
    ) {
        val calAnim by animateFloatAsState(targetValue = caloriesProgress, animationSpec = tween(1000))
        val hydAnim by animateFloatAsState(targetValue = hydrationProgress, animationSpec = tween(1000))

        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 14.dp.toPx()
            // Calories outer ring
            drawArc(tokens.border, 0f, 360f, false, style = Stroke(stroke, cap = StrokeCap.Round))
            drawArc(tokens.energyMark, -90f, 360f * calAnim, false, style = Stroke(stroke, cap = StrokeCap.Round))

            // Hydration inner ring
            val inset = stroke * 1.3f
            drawArc(tokens.border, 0f, 360f, false, style = Stroke(stroke * 0.7f, cap = StrokeCap.Round), topLeft = Offset(inset, inset), size = Size(size.width - inset * 2, size.height - inset * 2))
            drawArc(tokens.hydrationMark, -90f, 360f * hydAnim, false, style = Stroke(stroke * 0.7f, cap = StrokeCap.Round), topLeft = Offset(inset, inset), size = Size(size.width - inset * 2, size.height - inset * 2))
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = tokens.energyMark, modifier = Modifier.size(20.dp))
            Text(netCalories, style = MaterialTheme.typography.bodyMedium, color = tokens.text, fontWeight = FontWeight.Bold)
            Text(hydrationText, style = MaterialTheme.typography.bodySmall, color = tokens.hydrationMark)
        }
    }
}

@Composable
fun MacroPillRow() {
    val tokens = CaloTheme.tokens
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        MacroPill("Protein", "124g", tokens.proteinMark, tokens)
        MacroPill("Carbs", "210g", tokens.carbsMark, tokens)
        MacroPill("Fat", "68g", tokens.fatMark, tokens)
    }
}

@Composable
fun MacroPill(label: String, value: String, accent: Color, tokens: com.calovelo.app.presentation.theme.CaloTokens) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = tokens.surface,
        border = BorderStroke(1.dp, tokens.border),
        modifier = Modifier.width(100.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(accent)
            )
            Spacer(Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.bodySmall, color = tokens.muted)
            Text(value, style = MaterialTheme.typography.titleSmall, color = tokens.text, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun WeekStrip() {
    val tokens = CaloTheme.tokens
    val days = listOf("M" to true, "T" to true, "W" to true, "T" to true, "F" to false, "S" to false, "S" to false)
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = tokens.surface,
        border = BorderStroke(1.dp, tokens.border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            days.forEach { (day, active) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(day, style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                    Spacer(Modifier.height(6.dp))
                    Surface(
                        shape = CircleShape,
                        color = if (active) tokens.energyMark else tokens.border,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (active) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = tokens.onStart, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricMiniStat(label: String, value: String, tokens: com.calovelo.app.presentation.theme.CaloTokens) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = tokens.muted)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = tokens.text, fontWeight = FontWeight.Bold)
    }
}
