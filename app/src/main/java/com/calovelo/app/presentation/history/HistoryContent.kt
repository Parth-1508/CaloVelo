package com.calovelo.app.presentation.history

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.calovelo.app.presentation.theme.CaloTheme

@Composable
fun HistoryContent(
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
                    Text("ACTIVITY LOG", style = MaterialTheme.typography.labelMedium, color = tokens.muted, fontWeight = FontWeight.Bold)
                    Text("Workout History", style = MaterialTheme.typography.headlineMedium, color = tokens.text, fontWeight = FontWeight.Bold)
                }
                Badge(containerColor = tokens.greenStateContainer ?: tokens.surface) {
                    Text("This Month", color = tokens.energyMark, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                }
            }

            // Monthly Summary Card
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
                    Text("October Summary", style = MaterialTheme.typography.titleMedium, color = tokens.text, fontWeight = FontWeight.Bold)
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

            // Activity List
            val activities = listOf(
                ActivityEntry("Morning Tempo Run", "Today, 7:30 AM", "8.42 km", "42:15", "610 kcal", Icons.Default.DirectionsRun, tokens.energyMark),
                ActivityEntry("Evening Recovery Ride", "Yesterday, 6:00 PM", "22.1 km", "55:30", "480 kcal", Icons.Default.DirectionsBike, tokens.hydrationMark),
                ActivityEntry("Interval Sprint Session", "Oct 28, 6:45 AM", "6.8 km", "32:10", "520 kcal", Icons.Default.DirectionsRun, tokens.nutritionMark),
                ActivityEntry("Long Sunday Ride", "Oct 27, 8:00 AM", "65.3 km", "2:45:00", "1,840 kcal", Icons.Default.DirectionsBike, tokens.aiMark),
                ActivityEntry("Hill Repeats", "Oct 26, 7:00 AM", "5.2 km", "38:20", "440 kcal", Icons.Default.DirectionsRun, tokens.proteinMark),
                ActivityEntry("Easy Recovery Walk", "Oct 25, 12:00 PM", "3.8 km", "45:00", "180 kcal", Icons.Default.DirectionsWalk, tokens.muted),
            )

            activities.forEach { activity ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = tokens.surface,
                    border = BorderStroke(1.dp, tokens.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = activity.accentColor.copy(alpha = 0.15f),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(activity.icon, contentDescription = null, tint = activity.accentColor, modifier = Modifier.size(22.dp))
                                    }
                                }
                                Column {
                                    Text(activity.title, style = MaterialTheme.typography.titleSmall, color = tokens.text, fontWeight = FontWeight.Bold)
                                    Text(activity.date, style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                                }
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = tokens.muted)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            HistoryMetric("Distance", activity.distance, tokens)
                            HistoryMetric("Duration", activity.duration, tokens)
                            HistoryMetric("Calories", activity.calories, tokens)
                        }
                    }
                }
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
        Text(value, style = MaterialTheme.typography.titleMedium, color = tokens.text, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall, color = tokens.muted)
    }
}

@Composable
private fun HistoryMetric(
    label: String,
    value: String,
    tokens: com.calovelo.app.presentation.theme.CaloTokens
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = tokens.muted)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = tokens.text, fontWeight = FontWeight.Bold)
    }
}
