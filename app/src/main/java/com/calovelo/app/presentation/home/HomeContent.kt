package com.calovelo.app.presentation.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
    userName: String = "Athlete",
    dailyCalorieGoal: Int = 2500,
    dailyWaterGoalMl: Int = 3000,
    waterIntakeMl: Int = 0,
    onStartWorkout: () -> Unit = {},
    onOpenScanner: () -> Unit = {},
    onQuickAddWater: (Int) -> Unit = {},
    onOpenHydration: () -> Unit = {}
) {
    val tokens = CaloTheme.tokens
    val scrollState = rememberScrollState()

    val hydrationProgress = if (dailyWaterGoalMl > 0) (waterIntakeMl.toFloat() / dailyWaterGoalMl).coerceIn(0f, 1f) else 0f
    val hydrationPercent = if (dailyWaterGoalMl > 0) (waterIntakeMl * 100 / dailyWaterGoalMl) else 0

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
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ── Header ──────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TODAY'S OVERVIEW",
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
                        fontWeight = FontWeight.Black
                    )
                }
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(tokens.surface.copy(alpha = 0.6f))
                        .border(1.dp, tokens.border.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = tokens.text,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            // ── Quick Actions Row ───────────────────
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
                    icon = Icons.Default.CameraAlt,
                    text = "Scan",
                    color = tokens.surface,
                    textColor = tokens.text,
                    borderColor = tokens.border.copy(alpha = 0.4f),
                    onClick = onOpenScanner,
                    modifier = Modifier.weight(1f)
                )
                QuickActionPill(
                    icon = Icons.Default.FitnessCenter,
                    text = "Lift",
                    color = tokens.surface,
                    textColor = tokens.text,
                    borderColor = tokens.border.copy(alpha = 0.4f),
                    onClick = onStartWorkout,
                    modifier = Modifier.weight(1f)
                )
            }

            // ── Hydration Card ──────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(tokens.surface.copy(alpha = tokens.surfaceAlpha))
                    .border(1.dp, tokens.border.copy(alpha = 0.3f), RoundedCornerShape(28.dp))
                    .clickable { onOpenHydration() }
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mini water gauge
                    Box(modifier = Modifier.size(72.dp), contentAlignment = Alignment.Center) {
                        val hydAnim by animateFloatAsState(
                            targetValue = hydrationProgress,
                            animationSpec = tween(1000),
                            label = "hydration"
                        )
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val sw = 6.dp.toPx()
                            drawArc(
                                tokens.border.copy(alpha = 0.3f),
                                0f, 360f, false,
                                style = Stroke(sw, cap = StrokeCap.Round)
                            )
                            drawArc(
                                tokens.hydrationMark,
                                -90f, 360f * hydAnim, false,
                                style = Stroke(sw, cap = StrokeCap.Round)
                            )
                        }
                        Icon(
                            Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = tokens.hydrationMark,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Hydration",
                            style = MaterialTheme.typography.titleMedium,
                            color = tokens.text,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${waterIntakeMl}ml / ${dailyWaterGoalMl}ml · $hydrationPercent%",
                            style = MaterialTheme.typography.bodySmall,
                            color = tokens.muted
                        )
                    }

                    // Quick +250ml button
                    FilledTonalButton(
                        onClick = { onQuickAddWater(250) },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = tokens.waterContainer,
                            contentColor = tokens.waterInk
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("250ml", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // ── Theme-specific dashboard ────────────
            if (tokens.themeId == CaloThemeId.ThermalDark) {
                ThermalHomeVariant(onStartWorkout = onStartWorkout, onOpenScanner = onOpenScanner)
            } else {
                EmeraldHomeVariant(
                    dailyCalorieGoal = dailyCalorieGoal,
                    onStartWorkout = onStartWorkout,
                    onOpenScanner = onOpenScanner
                )
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
fun EmeraldHomeVariant(
    dailyCalorieGoal: Int,
    onStartWorkout: () -> Unit,
    onOpenScanner: () -> Unit
) {
    val tokens = CaloTheme.tokens

    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        // Energy overview card
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = tokens.surface.copy(alpha = tokens.surfaceAlpha),
            border = androidx.compose.foundation.BorderStroke(1.dp, tokens.border.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Today's Energy",
                        style = MaterialTheme.typography.titleMedium,
                        color = tokens.text,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .background(
                                (tokens.greenStateContainer
                                    ?: tokens.surface.copy(alpha = 0.5f)),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "Goal: $dailyCalorieGoal",
                            color = tokens.energyMark,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Summary message
                Text(
                    "Log meals from the Scan tab and check Insights for a full macro breakdown.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = tokens.muted
                )

                // Macro legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MacroLegendDot("Protein", tokens.proteinMark, tokens)
                    MacroLegendDot("Carbs", tokens.carbsMark, tokens)
                    MacroLegendDot("Fat", tokens.fatMark, tokens)
                }
            }
        }

        // Recent Activity
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                "RECENT ACTIVITY",
                style = MaterialTheme.typography.labelSmall,
                color = tokens.muted,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = tokens.surface.copy(alpha = tokens.surfaceAlpha),
                border = androidx.compose.foundation.BorderStroke(1.dp, tokens.border.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onStartWorkout)
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
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        tokens.energyMark.copy(alpha = 0.12f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.DirectionsRun,
                                    contentDescription = null,
                                    tint = tokens.energyMark,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    "Start a new workout",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = tokens.text,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "GPS run, ride or gym session",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = tokens.muted
                                )
                            }
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = tokens.muted
                        )
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
        // Glassmorphic Action Blocks
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            GlassCard(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onOpenScanner() },
                tokens = tokens
            ) {
                Icon(
                    Icons.Default.RestaurantMenu,
                    contentDescription = null,
                    tint = tokens.nutritionMark,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "Scan Meal",
                    style = MaterialTheme.typography.titleMedium,
                    color = tokens.text,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "AI macro analysis",
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.muted
                )
            }
            GlassCard(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onStartWorkout() },
                tokens = tokens
            ) {
                Icon(
                    Icons.Default.FlashOn,
                    contentDescription = null,
                    tint = tokens.energyMark,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "Train",
                    style = MaterialTheme.typography.titleMedium,
                    color = tokens.text,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Start activity",
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.muted
                )
            }
        }
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    tokens: com.calovelo.app.presentation.theme.CaloTokens,
    content: @Composable ColumnScope.() -> Unit
) {
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
fun QuickActionPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    color: Color,
    textColor: Color,
    borderColor: Color = Color.Transparent,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color)
            .then(
                if (borderColor != Color.Transparent)
                    Modifier.border(1.dp, borderColor, RoundedCornerShape(20.dp))
                else Modifier
            )
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = textColor, modifier = Modifier.size(18.dp))
            Text(text, color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
private fun MacroLegendDot(
    label: String,
    accent: Color,
    tokens: com.calovelo.app.presentation.theme.CaloTokens
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(accent)
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = tokens.muted)
    }
}
