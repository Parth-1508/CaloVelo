package com.calovelo.app.presentation.insights

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.calovelo.app.presentation.theme.CaloTheme

@Composable
fun InsightsContent(
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
                    Text("NUTRITION & INSIGHTS", style = MaterialTheme.typography.labelMedium, color = tokens.muted, fontWeight = FontWeight.Bold)
                    Text("7-Day Calorie Analysis", style = MaterialTheme.typography.headlineMedium, color = tokens.text, fontWeight = FontWeight.Bold)
                }
                Badge(containerColor = tokens.nutritionStateContainer ?: tokens.surface) {
                    Text("Avg 2,240 kcal", color = tokens.nutritionMark, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                }
            }

            // 7-Day Chart Card
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
                        Text("Calorie Intake vs Target", style = MaterialTheme.typography.titleMedium, color = tokens.text, fontWeight = FontWeight.Bold)
                        Text("Target: 2,500 kcal", style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                    }

                    // Bar Chart Canvas representation
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        val bars = listOf("Mon" to 2200f, "Tue" to 2450f, "Wed" to 2600f, "Thu" to 2100f, "Fri" to 2350f, "Sat" to 2800f, "Sun" to 2400f)
                        val maxVal = 3000f

                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            bars.forEach { (day, valKcal) ->
                                val heightFraction = (valKcal / maxVal).coerceIn(0.1f, 1f)
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom,
                                    modifier = Modifier.fillMaxHeight()
                                ) {
                                    Spacer(Modifier.weight(1f - heightFraction))
                                    Surface(
                                        shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp),
                                        color = if (valKcal > 2500f) tokens.nutritionMark else tokens.energyMark,
                                        modifier = Modifier
                                            .width(24.dp)
                                            .fillMaxHeight(heightFraction)
                                    ) {}
                                    Spacer(Modifier.height(8.dp))
                                    Text(day, style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                                }
                            }
                        }
                    }
                }
            }

            // Recent Meal Journal List
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
                    Text("Recent Meal Journal", style = MaterialTheme.typography.titleMedium, color = tokens.text, fontWeight = FontWeight.Bold)

                    MealJournalItem("Breakfast", "Oatmeal with Berries & Whey", "450 kcal · P: 32g · C: 55g · F: 8g", tokens)
                    HorizontalDivider(color = tokens.border)
                    MealJournalItem("Lunch", "Paneer Roti Bowl with Dal", "680 kcal · P: 42g · C: 72g · F: 22g", tokens)
                    HorizontalDivider(color = tokens.border)
                    MealJournalItem("Dinner", "Grilled Salmon & Quinoa", "720 kcal · P: 48g · C: 44g · F: 24g", tokens)
                }
            }
        }
    }
}

@Composable
fun MealJournalItem(mealType: String, title: String, macros: String, tokens: com.calovelo.app.presentation.theme.CaloTokens) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(mealType, style = MaterialTheme.typography.labelSmall, color = tokens.nutritionMark, fontWeight = FontWeight.Bold)
            Text(title, style = MaterialTheme.typography.titleSmall, color = tokens.text, fontWeight = FontWeight.SemiBold)
            Text(macros, style = MaterialTheme.typography.bodySmall, color = tokens.muted)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = tokens.muted)
    }
}
