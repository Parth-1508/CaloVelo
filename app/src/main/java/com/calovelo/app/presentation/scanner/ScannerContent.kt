package com.calovelo.app.presentation.scanner

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calovelo.app.presentation.theme.CaloTheme

@Composable
fun ScannerContent(
    modifier: Modifier = Modifier
) {
    val tokens = CaloTheme.tokens
    var portionWeight by remember { mutableStateOf(250f) }

    val baseCalories = 420
    val baseProtein = 28
    val baseCarbs = 45
    val baseFat = 14

    val multiplier = portionWeight / 250f
    val calories = (baseCalories * multiplier).toInt()
    val protein = (baseProtein * multiplier).toInt()
    val carbs = (baseCarbs * multiplier).toInt()
    val fat = (baseFat * multiplier).toInt()

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
                    Text("AI VISION SCANNER", style = MaterialTheme.typography.labelMedium, color = tokens.muted, fontWeight = FontWeight.Bold)
                    Text("Meal & Macro Detection", style = MaterialTheme.typography.headlineMedium, color = tokens.text, fontWeight = FontWeight.Bold)
                }
                Badge(containerColor = tokens.aiStateContainer ?: tokens.surface) {
                    Text("AI Ready", color = tokens.aiMark, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                }
            }

            // Viewfinder Placeholder with Bounding Boxes
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = tokens.surface,
                border = BorderStroke(1.dp, tokens.border),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(tokens.mapBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Viewfinder",
                        tint = tokens.muted.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )

                    // Simulated Bounding Boxes
                    Box(
                        modifier = Modifier
                            .offset(x = (-60).dp, y = (-30).dp)
                            .size(100.dp, 70.dp)
                            .border(2.dp, tokens.aiMark, RoundedCornerShape(8.dp))
                            .padding(4.dp),
                        contentAlignment = Alignment.TopStart
                    ) {
                        Surface(
                            color = tokens.aiMark,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("Paneer 96%", color = tokens.aiInk, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 4.dp))
                        }
                    }

                    Box(
                        modifier = Modifier
                            .offset(x = 70.dp, y = 20.dp)
                            .size(90.dp, 80.dp)
                            .border(2.dp, tokens.energyMark, RoundedCornerShape(8.dp))
                            .padding(4.dp),
                        contentAlignment = Alignment.TopStart
                    ) {
                        Surface(
                            color = tokens.energyMark,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("Roti 94%", color = tokens.onStart, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 4.dp))
                        }
                    }
                }
            }

            // Portion Adjustment Slider
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = tokens.surface,
                border = BorderStroke(1.dp, tokens.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Portion Adjustment", style = MaterialTheme.typography.titleSmall, color = tokens.text, fontWeight = FontWeight.Bold)
                        Text("${portionWeight.toInt()}g", style = MaterialTheme.typography.titleSmall, color = tokens.aiMark, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = portionWeight,
                        onValueChange = { portionWeight = it },
                        valueRange = 50f..500f,
                        steps = 8
                    )
                }
            }

            // Thermal MealDeck Macros
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
                        Text("Thermal MealDeck", style = MaterialTheme.typography.titleMedium, color = tokens.text, fontWeight = FontWeight.Bold)
                        Text("$calories kcal", style = MaterialTheme.typography.titleMedium, color = tokens.energyMark, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MacroNutrientCard("Protein", "${protein}g", tokens.proteinMark, tokens)
                        MacroNutrientCard("Carbs", "${carbs}g", tokens.carbsMark, tokens)
                        MacroNutrientCard("Fat", "${fat}g", tokens.fatMark, tokens)
                    }

                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(containerColor = tokens.aiMark, contentColor = tokens.aiInk),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Log Meal to Journal")
                    }
                }
            }
        }
    }
}

@Composable
fun MacroNutrientCard(label: String, value: String, accent: Color, tokens: com.calovelo.app.presentation.theme.CaloTokens) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = tokens.background,
        border = BorderStroke(1.dp, tokens.border),
        modifier = Modifier.width(100.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(accent)
            )
            Spacer(Modifier.height(6.dp))
            Text(label, style = MaterialTheme.typography.bodySmall, color = tokens.muted)
            Text(value, style = MaterialTheme.typography.titleSmall, color = tokens.text, fontWeight = FontWeight.Bold)
        }
    }
}
