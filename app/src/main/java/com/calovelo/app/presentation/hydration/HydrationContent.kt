package com.calovelo.app.presentation.hydration

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calovelo.app.presentation.theme.CaloTheme

@Composable
fun HydrationContent(
    modifier: Modifier = Modifier,
    intakeMl: Int = 2100,
    goalMl: Int = 3000,
    onQuickAdd: (Int) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val tokens = CaloTheme.tokens
    val progress = if (goalMl > 0) (intakeMl.toFloat() / goalMl).coerceIn(0f, 1f) else 0f
    val percent = if (goalMl > 0) (intakeMl * 100 / goalMl) else 0
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(800),
        label = "hydrationProgress"
    )

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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "HYDRATION",
                        style = MaterialTheme.typography.labelSmall,
                        color = tokens.hydrationMark,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        "Water Intake",
                        style = MaterialTheme.typography.headlineMedium,
                        color = tokens.text,
                        fontWeight = FontWeight.Black
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(tokens.waterContainer)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        "$percent%",
                        color = tokens.waterInk,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            // ── Main Gauge Card ─────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(tokens.surface.copy(alpha = tokens.surfaceAlpha))
                    .border(1.dp, tokens.border.copy(alpha = 0.3f), RoundedCornerShape(28.dp))
                    .padding(28.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Box(
                        modifier = Modifier.size(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val stroke = 16.dp.toPx()
                            drawArc(
                                color = tokens.border.copy(alpha = 0.3f),
                                startAngle = 135f,
                                sweepAngle = 270f,
                                useCenter = false,
                                style = Stroke(stroke, cap = StrokeCap.Round)
                            )
                            drawArc(
                                color = tokens.hydrationMark,
                                startAngle = 135f,
                                sweepAngle = 270f * animatedProgress,
                                useCenter = false,
                                style = Stroke(stroke, cap = StrokeCap.Round)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.WaterDrop,
                                contentDescription = null,
                                tint = tokens.hydrationMark,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "$intakeMl ml",
                                style = MaterialTheme.typography.headlineMedium,
                                color = tokens.text,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                "of $goalMl ml",
                                style = MaterialTheme.typography.bodySmall,
                                color = tokens.muted
                            )
                        }
                    }

                    // Quick add buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(150, 250, 350, 500).forEach { amount ->
                            OutlinedButton(
                                onClick = { onQuickAdd(amount) },
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    tokens.hydrationMark.copy(alpha = 0.6f)
                                ),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = tokens.hydrationMark
                                ),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    "+${amount}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // ── Today's Log Card ────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(tokens.surface.copy(alpha = tokens.surfaceAlpha))
                    .border(1.dp, tokens.border.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = tokens.hydrationMark,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            "Today's Log",
                            style = MaterialTheme.typography.titleMedium,
                            color = tokens.text,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    val logs = listOf(
                        "7:30 AM" to "250 ml — Morning",
                        "9:15 AM" to "350 ml — Post-Workout",
                        "12:00 PM" to "500 ml — Lunch",
                        "3:30 PM" to "250 ml — Afternoon",
                        "5:45 PM" to "350 ml — Pre-Dinner",
                        "8:00 PM" to "400 ml — Evening"
                    )

                    logs.forEach { (time, detail) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                time,
                                style = MaterialTheme.typography.bodySmall,
                                color = tokens.muted,
                                modifier = Modifier.width(72.dp)
                            )
                            Text(
                                detail,
                                style = MaterialTheme.typography.bodyMedium,
                                color = tokens.text,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                Icons.Default.WaterDrop,
                                contentDescription = null,
                                tint = tokens.hydrationMark.copy(alpha = 0.5f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        if (logs.last().first != time) {
                            HorizontalDivider(color = tokens.border.copy(alpha = 0.2f))
                        }
                    }
                }
            }

            // ── Hydration Tip ───────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(tokens.waterContainer)
                    .border(1.dp, tokens.border.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = tokens.waterInk,
                        modifier = Modifier.size(24.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Hydration Tip",
                            style = MaterialTheme.typography.titleSmall,
                            color = tokens.waterInk,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Drink 500 ml within 30 minutes of finishing your workout for optimal recovery.",
                            style = MaterialTheme.typography.bodySmall,
                            color = tokens.text
                        )
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}
