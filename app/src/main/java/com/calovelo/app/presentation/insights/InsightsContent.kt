package com.calovelo.app.presentation.insights

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calovelo.app.presentation.theme.CaloTheme

@Composable
fun InsightsContent(
    modifier: Modifier = Modifier
) {
    val tokens = CaloTheme.tokens

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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "NUTRITION & INSIGHTS", 
                        style = MaterialTheme.typography.labelSmall, 
                        color = tokens.nutritionMark, 
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "7-Day Analysis", 
                        style = MaterialTheme.typography.headlineMedium, 
                        color = tokens.text, 
                        fontWeight = FontWeight.Black,
                        fontSize = 32.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background((tokens.nutritionStateContainer ?: tokens.surface).copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("Avg 2,240 kcal", color = tokens.nutritionMark, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                }
            }

            // 7-Day Chart Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(tokens.surface.copy(alpha = 0.4f))
                    .border(1.dp, tokens.border.copy(alpha = 0.3f), RoundedCornerShape(32.dp))
                    .padding(24.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Intake vs Target", style = MaterialTheme.typography.titleMedium, color = tokens.text, fontWeight = FontWeight.Bold)
                        Text("Target: 2,500", style = MaterialTheme.typography.labelMedium, color = tokens.muted)
                    }

                    // Bar Chart
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
                                    Box(
                                        modifier = Modifier
                                            .width(20.dp)
                                            .fillMaxHeight(heightFraction)
                                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = if (valKcal > 2500f) listOf(tokens.nutritionMark, tokens.nutritionMark.copy(alpha = 0.5f))
                                                             else listOf(tokens.energyMark, tokens.energyMark.copy(alpha = 0.5f))
                                                )
                                            )
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    Text(day, style = MaterialTheme.typography.labelSmall, color = tokens.muted)
                                }
                            }
                        }
                    }
                }
            }

            // Recent Meal Journal List
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("RECENT MEAL JOURNAL", style = MaterialTheme.typography.labelMedium, color = tokens.muted, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(tokens.surface.copy(alpha = 0.4f))
                        .border(1.dp, tokens.border.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        MealJournalItem("Breakfast", "Oatmeal with Berries & Whey", "450 kcal · P: 32g · C: 55g · F: 8g", tokens)
                        HorizontalDivider(color = tokens.border.copy(alpha = 0.3f))
                        MealJournalItem("Lunch", "Paneer Roti Bowl with Dal", "680 kcal · P: 42g · C: 72g · F: 22g", tokens)
                        HorizontalDivider(color = tokens.border.copy(alpha = 0.3f))
                        MealJournalItem("Dinner", "Grilled Salmon & Quinoa", "720 kcal · P: 48g · C: 44g · F: 24g", tokens)
                    }
                }
            }
            
            Spacer(Modifier.height(80.dp))
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
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(tokens.nutritionMark.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Restaurant, contentDescription = null, tint = tokens.nutritionMark, modifier = Modifier.size(24.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(mealType, style = MaterialTheme.typography.labelSmall, color = tokens.nutritionMark, fontWeight = FontWeight.Bold)
                Text(title, style = MaterialTheme.typography.titleMedium, color = tokens.text, fontWeight = FontWeight.Bold)
                Text(macros, style = MaterialTheme.typography.bodySmall, color = tokens.muted)
            }
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = tokens.muted.copy(alpha = 0.5f))
    }
}
