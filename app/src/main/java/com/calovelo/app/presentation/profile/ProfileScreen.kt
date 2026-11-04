package com.calovelo.app.presentation.profile

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            // Header with back
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(tokens.surface.copy(alpha = 0.4f))
                        .border(1.dp, tokens.border.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = tokens.text)
                    }
                }
                Column {
                    Text("ATHLETE PROFILE", style = MaterialTheme.typography.labelSmall, color = tokens.muted, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Edit Profile", style = MaterialTheme.typography.headlineMedium, color = tokens.text, fontWeight = FontWeight.Black, fontSize = 32.sp)
                }
            }

            // Avatar Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(tokens.surface.copy(alpha = 0.4f))
                    .border(1.dp, tokens.border.copy(alpha = 0.3f), RoundedCornerShape(32.dp))
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(tokens.greenStateContainer ?: tokens.surface)
                            .border(2.dp, tokens.energyMark.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = tokens.energyMark, modifier = Modifier.size(56.dp))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(userName, style = MaterialTheme.typography.headlineSmall, color = tokens.text, fontWeight = FontWeight.Black)
                        Text("CaloVelo Member", style = MaterialTheme.typography.labelMedium, color = tokens.energyMark, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                }
            }

            // Stats Summary Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(tokens.surface.copy(alpha = 0.4f))
                    .border(1.dp, tokens.border.copy(alpha = 0.3f), RoundedCornerShape(32.dp))
                    .padding(24.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    Text("LIFETIME STATS", style = MaterialTheme.typography.labelMedium, color = tokens.muted, fontWeight = FontWeight.Black, letterSpacing = 1.sp)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        ProfileStatItem("Total Runs", "247", tokens)
                        ProfileStatItem("Distance", "1,842 km", tokens)
                        ProfileStatItem("Calories", "148K", tokens)
                    }
                }
            }

            // Personal Information Inputs
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(tokens.surface.copy(alpha = 0.4f))
                    .border(1.dp, tokens.border.copy(alpha = 0.3f), RoundedCornerShape(32.dp))
                    .padding(24.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    Text("METRICS", style = MaterialTheme.typography.labelMedium, color = tokens.muted, fontWeight = FontWeight.Black, letterSpacing = 1.sp)

                    OutlinedTextField(
                        value = nameField,
                        onValueChange = { nameField = it },
                        label = { Text("Display Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = tokens.muted) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = tokens.energyMark,
                            unfocusedBorderColor = tokens.border.copy(alpha = 0.5f),
                            focusedLabelColor = tokens.energyMark,
                            unfocusedLabelColor = tokens.muted,
                            cursorColor = tokens.energyMark,
                            focusedTextColor = tokens.text,
                            unfocusedTextColor = tokens.text,
                            unfocusedContainerColor = tokens.surface.copy(alpha = 0.2f),
                            focusedContainerColor = tokens.surface.copy(alpha = 0.2f)
                        )
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = heightField,
                            onValueChange = { heightField = it },
                            label = { Text("Height (cm)") },
                            leadingIcon = { Icon(Icons.Default.Height, contentDescription = null, tint = tokens.muted) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = tokens.energyMark, unfocusedBorderColor = tokens.border.copy(alpha=0.5f),
                                focusedLabelColor = tokens.energyMark, unfocusedLabelColor = tokens.muted,
                                cursorColor = tokens.energyMark, focusedTextColor = tokens.text, unfocusedTextColor = tokens.text,
                                unfocusedContainerColor = tokens.surface.copy(alpha = 0.2f), focusedContainerColor = tokens.surface.copy(alpha = 0.2f)
                            )
                        )
                        OutlinedTextField(
                            value = weightField,
                            onValueChange = { weightField = it },
                            label = { Text("Weight (kg)") },
                            leadingIcon = { Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = tokens.muted) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = tokens.energyMark, unfocusedBorderColor = tokens.border.copy(alpha=0.5f),
                                focusedLabelColor = tokens.energyMark, unfocusedLabelColor = tokens.muted,
                                cursorColor = tokens.energyMark, focusedTextColor = tokens.text, unfocusedTextColor = tokens.text,
                                unfocusedContainerColor = tokens.surface.copy(alpha = 0.2f), focusedContainerColor = tokens.surface.copy(alpha = 0.2f)
                            )
                        )
                    }
                }
            }

            // Save Button
            Button(
                onClick = {
                    onSave(nameField.text, heightField.text.toIntOrNull() ?: heightCm, weightField.text.toFloatOrNull() ?: weightKg)
                },
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = tokens.startFill, contentColor = tokens.onStart),
                modifier = Modifier.fillMaxWidth().height(64.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Text("Save Profile", fontSize = 18.sp, fontWeight = FontWeight.Black)
            }
            
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun ProfileStatItem(label: String, value: String, tokens: com.calovelo.app.presentation.theme.CaloTokens) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = tokens.text, fontWeight = FontWeight.Black)
        Text(label, style = MaterialTheme.typography.labelMedium, color = tokens.muted, letterSpacing = 0.5.sp)
    }
}
