package com.calovelo.app.presentation.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calovelo.app.presentation.theme.CaloTheme

@Composable
fun HistoryContent(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {}
) {
    val tokens = CaloTheme.tokens
    var selectedFilter by remember { mutableStateOf("All") }
    val filterTabs = listOf("All", "Runs", "Rides", "Gym")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(tokens.background, tokens.surface.copy(alpha = 0.15f), tokens.background)
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Top Bar with Back ───────────────────
            item {
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
                            "ACTIVITY LOG",
                            style = MaterialTheme.typography.labelSmall,
                            color = tokens.energyMark,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            "Workout History",
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
                        Text("October", color = tokens.energyMark, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            // ── Monthly Summary Card ────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(tokens.surface.copy(alpha = tokens.surfaceAlpha))
                        .border(1.dp, tokens.border.copy(alpha = 0.3f), RoundedCornerShape(28.dp))
                        .padding(24.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Monthly Summary", style = MaterialTheme.typography.titleMedium, color = tokens.text, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            HistorySummaryItem("Workouts", "23", tokens.energyMark, tokens)
                            HistorySummaryItem("Distance", "186 km", tokens.hydrationMark, tokens)
                            HistorySummaryItem("Time", "18h 42m", tokens.nutritionMark, tokens)
                            HistorySummaryItem("Calories", "14,200", tokens.proteinMark, tokens)
                        }
                    }
                }
            }

            // ── Activity Filter Chips ───────────────
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filterTabs) { tab ->
                        val isSelected = selectedFilter == tab
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = tab },
                            label = { Text(tab, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = tokens.energyMark,
                                selectedLabelColor = tokens.onStart,
                                containerColor = tokens.surface.copy(alpha = 0.5f),
                                labelColor = tokens.text
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // ── Activities List ─────────────────────
            val allActivities = listOf(
                ActivityEntry("Morning Tempo Run", "Today, 7:30 AM", "8.42 km", "42:15", "610 kcal", "Run", Icons.Default.DirectionsRun, tokens.energyMark),
                ActivityEntry("Heavy Leg Day & Squats", "Today, 6:00 AM", "12 sets", "52:00", "420 kcal", "Gym", Icons.Default.FitnessCenter, tokens.proteinMark),
                ActivityEntry("Evening Recovery Ride", "Yesterday, 6:00 PM", "22.1 km", "55:30", "480 kcal", "Ride", Icons.Default.DirectionsBike, tokens.hydrationMark),
                ActivityEntry("Interval Sprint Session", "Oct 28, 6:45 AM", "6.8 km", "32:10", "520 kcal", "Run", Icons.Default.DirectionsRun, tokens.nutritionMark),
                ActivityEntry("Chest & Triceps Hypertrophy", "Oct 28, 5:00 PM", "16 sets", "48:15", "380 kcal", "Gym", Icons.Default.FitnessCenter, tokens.proteinMark),
                ActivityEntry("Long Sunday Ride", "Oct 27, 8:00 AM", "65.3 km", "2:45:00", "1,840 kcal", "Ride", Icons.Default.DirectionsBike, tokens.aiMark),
                ActivityEntry("Hill Repeats", "Oct 26, 7:00 AM", "5.2 km", "38:20", "440 kcal", "Run", Icons.Default.DirectionsRun, tokens.proteinMark)
            )

            val filtered = allActivities.filter {
                when (selectedFilter) {
                    "Runs" -> it.category == "Run"
                    "Rides" -> it.category == "Ride"
                    "Gym" -> it.category == "Gym"
                    else -> true
                }
            }

            items(filtered) { activity ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(tokens.surface.copy(alpha = tokens.surfaceAlpha))
                        .border(1.dp, tokens.border.copy(alpha = 0.25f), RoundedCornerShape(22.dp))
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(activity.accentColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(activity.icon, contentDescription = null, tint = activity.accentColor, modifier = Modifier.size(22.dp))
                                }
                                Column {
                                    Text(activity.title, style = MaterialTheme.typography.titleMedium, color = tokens.text, fontWeight = FontWeight.Bold)
                                    Text(activity.date, style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = tokens.muted.copy(alpha = 0.6f))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            HistoryMetric("Volume / Dist", activity.distance, tokens)
                            HistoryMetric("Duration", activity.duration, tokens)
                            HistoryMetric("Calories", activity.calories, tokens)
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(60.dp))
            }
        }
    }
}

private data class ActivityEntry(
    val title: String,
    val date: String,
    val distance: String,
    val duration: String,
    val calories: String,
    val category: String,
    val icon: ImageVector,
    val accentColor: Color
)

@Composable
private fun HistorySummaryItem(
    label: String,
    value: String,
    accent: Color,
    tokens: com.calovelo.app.presentation.theme.CaloTokens
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(accent)
        )
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, color = tokens.text, fontWeight = FontWeight.Black)
        Text(label, style = MaterialTheme.typography.labelSmall, color = tokens.muted)
    }
}

@Composable
private fun HistoryMetric(
    label: String,
    value: String,
    tokens: com.calovelo.app.presentation.theme.CaloTokens
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleSmall, color = tokens.text, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = tokens.muted)
    }
}
