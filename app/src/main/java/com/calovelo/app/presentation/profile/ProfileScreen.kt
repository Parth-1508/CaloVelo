package com.calovelo.app.presentation.profile

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.calovelo.app.presentation.theme.CaloTheme

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    userName: String = "Athlete",
    heightCm: Int = 175,
    weightKg: Float = 70f,
    onSave: (name: String, height: Int, weight: Float) -> Unit = { _, _, _ -> },
    onNavigateBack: () -> Unit = {}
) {
    val tokens = CaloTheme.tokens

    var nameField by remember(userName) { mutableStateOf(TextFieldValue(userName)) }
    var heightField by remember(heightCm) { mutableStateOf(TextFieldValue(heightCm.toString())) }
    var weightField by remember(weightKg) { mutableStateOf(TextFieldValue(String.format("%.1f", weightKg))) }

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
            // Header with back
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = tokens.text)
                }
                Column {
                    Text("ATHLETE PROFILE", style = MaterialTheme.typography.labelMedium, color = tokens.muted, fontWeight = FontWeight.Bold)
                    Text("Edit Profile", style = MaterialTheme.typography.headlineMedium, color = tokens.text, fontWeight = FontWeight.Bold)
                }
            }

            // Avatar Card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = tokens.surface,
                border = BorderStroke(1.dp, tokens.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = tokens.greenStateContainer ?: tokens.surface,
                        border = BorderStroke(3.dp, tokens.energyMark),
                        modifier = Modifier.size(96.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = tokens.energyMark, modifier = Modifier.size(48.dp))
                        }
                    }
                    Text(userName, style = MaterialTheme.typography.titleLarge, color = tokens.text, fontWeight = FontWeight.Bold)
                    Text("CaloVelo Member", style = MaterialTheme.typography.bodyMedium, color = tokens.muted)
                }
            }

            // Personal Information Card
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
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = tokens.aiMark)
                        Text("Personal Information", style = MaterialTheme.typography.titleMedium, color = tokens.text, fontWeight = FontWeight.Bold)
                    }

                    OutlinedTextField(
                        value = nameField,
                        onValueChange = { nameField = it },
                        label = { Text("Display Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = tokens.muted) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = tokens.energyMark,
                            unfocusedBorderColor = tokens.border,
                            focusedLabelColor = tokens.energyMark,
                            unfocusedLabelColor = tokens.muted,
                            cursorColor = tokens.energyMark,
                            focusedTextColor = tokens.text,
                            unfocusedTextColor = tokens.text
                        )
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = heightField,
                            onValueChange = { heightField = it },
                            label = { Text("Height (cm)") },
                            leadingIcon = { Icon(Icons.Default.Height, contentDescription = null, tint = tokens.muted) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = tokens.energyMark, unfocusedBorderColor = tokens.border,
                                focusedLabelColor = tokens.energyMark, unfocusedLabelColor = tokens.muted,
                                cursorColor = tokens.energyMark, focusedTextColor = tokens.text, unfocusedTextColor = tokens.text
                            )
                        )
                        OutlinedTextField(
                            value = weightField,
                            onValueChange = { weightField = it },
                            label = { Text("Weight (kg)") },
                            leadingIcon = { Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = tokens.muted) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = tokens.energyMark, unfocusedBorderColor = tokens.border,
                                focusedLabelColor = tokens.energyMark, unfocusedLabelColor = tokens.muted,
                                cursorColor = tokens.energyMark, focusedTextColor = tokens.text, unfocusedTextColor = tokens.text
                            )
                        )
                    }
                }
            }

            // Stats Summary Card
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
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.BarChart, contentDescription = null, tint = tokens.nutritionMark)
                        Text("Lifetime Stats", style = MaterialTheme.typography.titleMedium, color = tokens.text, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        ProfileStatItem("Total Runs", "247", tokens)
                        ProfileStatItem("Distance", "1,842 km", tokens)
                        ProfileStatItem("Calories", "148K kcal", tokens)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        ProfileStatItem("Avg Pace", "5:12 /km", tokens)
                        ProfileStatItem("Streak", "14 days", tokens)
                        ProfileStatItem("Level", "Pro", tokens)
                    }
                }
            }

            // Save Button
            Button(
                onClick = {
                    onSave(nameField.text, heightField.text.toIntOrNull() ?: heightCm, weightField.text.toFloatOrNull() ?: weightKg)
                },
                colors = ButtonDefaults.buttonColors(containerColor = tokens.startFill, contentColor = tokens.onStart),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Save Changes")
            }
        }
    }
}

@Composable
private fun ProfileStatItem(label: String, value: String, tokens: com.calovelo.app.presentation.theme.CaloTokens) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, color = tokens.text, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall, color = tokens.muted)
    }
}
