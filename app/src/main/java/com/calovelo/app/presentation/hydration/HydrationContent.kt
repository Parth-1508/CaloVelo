package com.calovelo.app.presentation.hydration

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.calovelo.app.presentation.theme.CaloTheme

@Composable
fun HydrationContent(
    modifier: Modifier = Modifier,
    intakeMl: Int = 2100,
    goalMl: Int = 3000,
    onQuickAdd: (Int) -> Unit = {}
) {
    val tokens = CaloTheme.tokens
    val progress = if (goalMl > 0) (intakeMl.toFloat() / goalMl).coerceIn(0f, 1f) else 0f
    val percent = if (goalMl > 0) (intakeMl * 100f / goalMl).toInt() else 0
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(800))

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
                    Text("HYDRATION TRACKER", style = MaterialTheme.typography.labelMedium, color = tokens.muted, fontWeight = FontWeight.Bold)
                    Text("Water Intake", style = MaterialTheme.typography.headlineMedium, color = tokens.text, fontWeight = FontWeight.Bold)
                }
                Badge(containerColor = tokens.waterContainer) {
                    Text("${percent}%", color = tokens.waterInk, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                }
            }

            // Main Gauge Card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = tokens.surface,
                border = BorderStroke(1.dp, tokens.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier.size(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val stroke = 16.dp.toPx()
                            drawArc(
                                color = tokens.border,
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
                            Text(
                                "${intakeMl} ml",
                                style = MaterialTheme.typography.headlineMedium,
                                color = tokens.text,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "of ${goalMl} ml",
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
                                border = BorderStroke(1.dp, tokens.hydrationMark),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = tokens.hydrationMark),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                            ) {
                                Text("+${amount}ml", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            // Today's Log Card
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = tokens.hydrationMark)
                        Text("Today's Log", style = MaterialTheme.typography.titleMedium, color = tokens.text, fontWeight = FontWeight.Bold)
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
                            Text(time, style = MaterialTheme.typography.bodySmall, color = tokens.muted, modifier = Modifier.width(72.dp))
                            Text(detail, style = MaterialTheme.typography.bodyMedium, color = tokens.text, modifier = Modifier.weight(1f))
                            Icon(Icons.Default.WaterDrop, contentDescription = null, tint = tokens.hydrationMark, modifier = Modifier.size(16.dp))
                        }
                        if (logs.last().first != time) {
                            HorizontalDivider(color = tokens.border.copy(alpha = 0.5f))
                        }
                    }
                }
            }

            // Hydration Tips
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = tokens.waterContainer,
                border = BorderStroke(1.dp, tokens.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = tokens.waterInk, modifier = Modifier.size(28.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Hydration Tip", style = MaterialTheme.typography.titleSmall, color = tokens.waterInk, fontWeight = FontWeight.Bold)
                        Text("Drink 500 ml within 30 minutes of finishing your workout for optimal recovery.", style = MaterialTheme.typography.bodySmall, color = tokens.text)
                    }
                }
            }
        }
    }
}
