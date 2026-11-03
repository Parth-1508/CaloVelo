package com.calovelo.app.presentation.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
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
import com.calovelo.app.presentation.theme.CaloThemeId

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    currentThemeId: CaloThemeId = CaloThemeId.EmeraldDark,
    remindersEnabled: Boolean = false,
    onThemeSelected: (CaloThemeId) -> Unit = {},
    onRemindersToggled: (Boolean) -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onOpenGoals: () -> Unit = {},
    onOpenPrivacyPolicy: () -> Unit = {},
    onSignOut: () -> Unit = {}
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
                    Text("PREFERENCES", style = MaterialTheme.typography.labelMedium, color = tokens.muted, fontWeight = FontWeight.Bold)
                    Text("Settings", style = MaterialTheme.typography.headlineMedium, color = tokens.text, fontWeight = FontWeight.Bold)
                }
                Surface(
                    shape = CircleShape,
                    color = tokens.surface,
                    border = BorderStroke(1.dp, tokens.border),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = tokens.text)
                    }
                }
            }

            // Theme Selection Card
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
                        Icon(Icons.Default.Palette, contentDescription = null, tint = tokens.energyMark)
                        Text("App Theme", style = MaterialTheme.typography.titleMedium, color = tokens.text, fontWeight = FontWeight.Bold)
                    }

                    CaloThemeId.entries.forEach { themeId ->
                        val isSelected = themeId == currentThemeId
                        val (label, description, accentColor) = when (themeId) {
                            CaloThemeId.EmeraldDark -> Triple("Emerald Dark", "Dark background with emerald green accents", Color(0xFF00E676))
                            CaloThemeId.EmeraldLight -> Triple("Emerald Light", "Light background with teal green accents", Color(0xFF059669))
                            CaloThemeId.ThermalDark -> Triple("Thermal Dark", "Ultra-dark with thermal orange & violet accents", Color(0xFFFF5733))
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) (tokens.greenStateContainer ?: tokens.surface) else tokens.background,
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) accentColor else tokens.border
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onThemeSelected(themeId) }
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(accentColor))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(label, style = MaterialTheme.typography.titleSmall, color = tokens.text, fontWeight = FontWeight.Bold)
                                    Text(description, style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = accentColor, modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Notifications Card
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
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = tokens.hydrationMark)
                        Text("Notifications", style = MaterialTheme.typography.titleMedium, color = tokens.text, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text("Hydration Reminders", style = MaterialTheme.typography.bodyLarge, color = tokens.text)
                            Text("Nudges between 8 AM and 10 PM when you fall behind pace", style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                        }
                        Switch(
                            checked = remindersEnabled,
                            onCheckedChange = onRemindersToggled,
                            colors = SwitchDefaults.colors(checkedTrackColor = tokens.energyMark, checkedThumbColor = tokens.onStart)
                        )
                    }
                }
            }

            // Quick Links
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = tokens.surface,
                border = BorderStroke(1.dp, tokens.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(4.dp)) {
                    SettingsLinkRow(icon = Icons.Default.Person, label = "Edit Profile", tint = tokens.aiMark, textColor = tokens.text, mutedColor = tokens.muted, onClick = onOpenProfile)
                    HorizontalDivider(color = tokens.border, modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsLinkRow(icon = Icons.Default.Flag, label = "Goals & Targets", tint = tokens.nutritionMark, textColor = tokens.text, mutedColor = tokens.muted, onClick = onOpenGoals)
                    HorizontalDivider(color = tokens.border, modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsLinkRow(icon = Icons.Default.Shield, label = "Privacy Policy", tint = tokens.hydrationMark, textColor = tokens.text, mutedColor = tokens.muted, onClick = onOpenPrivacyPolicy)
                    HorizontalDivider(color = tokens.border, modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsLinkRow(icon = Icons.AutoMirrored.Filled.Logout, label = "Sign Out", tint = tokens.proteinMark, textColor = tokens.text, mutedColor = tokens.muted, onClick = onSignOut)
                }
            }

            // App Info
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = tokens.surface,
                border = BorderStroke(1.dp, tokens.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("CaloVelo", style = MaterialTheme.typography.titleMedium, color = tokens.text, fontWeight = FontWeight.Bold)
                    Text("Version 2.0.0", style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                    Text("Fuel your ride. Own your data.", style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                }
            }
        }
    }
}

@Composable
private fun SettingsLinkRow(
    icon: ImageVector,
    label: String,
    tint: Color,
    textColor: Color,
    mutedColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, color = textColor, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = mutedColor)
    }
}
