package com.calovelo.app.presentation.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                    Text("PREFERENCES", style = MaterialTheme.typography.labelSmall, color = tokens.muted, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Settings", style = MaterialTheme.typography.headlineMedium, color = tokens.text, fontWeight = FontWeight.Black, fontSize = 32.sp)
                }
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(tokens.surface.copy(alpha = 0.4f))
                        .border(1.dp, tokens.border.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = tokens.text, modifier = Modifier.size(28.dp))
                }
            }

            // Theme Selection
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(tokens.surface.copy(alpha = 0.4f))
                    .border(1.dp, tokens.border.copy(alpha = 0.3f), RoundedCornerShape(32.dp))
                    .padding(24.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    Text("APP THEME", style = MaterialTheme.typography.labelMedium, color = tokens.muted, fontWeight = FontWeight.Black, letterSpacing = 1.sp)

                    CaloThemeId.entries.forEach { themeId ->
                        val isSelected = themeId == currentThemeId
                        val (label, description, accentColor) = when (themeId) {
                            CaloThemeId.EmeraldDark -> Triple("Emerald Dark", "Dark background with emerald green accents", Color(0xFF00E676))
                            CaloThemeId.EmeraldLight -> Triple("Emerald Light", "Light background with teal green accents", Color(0xFF059669))
                            CaloThemeId.ThermalDark -> Triple("Thermal Dark", "Ultra-dark with thermal orange & violet accents", Color(0xFFFF5733))
                        }
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) tokens.greenStateContainer ?: tokens.surface.copy(alpha=0.6f) else Color.Transparent)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) accentColor else tokens.border.copy(alpha=0.3f),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable { onThemeSelected(themeId) }
                                .padding(16.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(accentColor))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(label, style = MaterialTheme.typography.titleMedium, color = tokens.text, fontWeight = FontWeight.Bold)
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

            // Quick Links & Notifications
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(tokens.surface.copy(alpha = 0.4f))
                    .border(1.dp, tokens.border.copy(alpha = 0.3f), RoundedCornerShape(32.dp))
                    .padding(8.dp)
            ) {
                Column {
                    SettingsLinkRow(icon = Icons.Default.Person, label = "Edit Profile", tint = tokens.aiMark, tokens = tokens, onClick = onOpenProfile)
                    HorizontalDivider(color = tokens.border.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 24.dp))
                    SettingsLinkRow(icon = Icons.Default.Flag, label = "Goals & Targets", tint = tokens.nutritionMark, tokens = tokens, onClick = onOpenGoals)
                    HorizontalDivider(color = tokens.border.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 24.dp))
                    
                    // Notifications Toggle (Inline)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = tokens.hydrationMark, modifier = Modifier.size(24.dp))
                            Text("Hydration Reminders", style = MaterialTheme.typography.titleMedium, color = tokens.text, fontWeight = FontWeight.SemiBold)
                        }
                        Switch(
                            checked = remindersEnabled,
                            onCheckedChange = onRemindersToggled,
                            colors = SwitchDefaults.colors(checkedTrackColor = tokens.energyMark, checkedThumbColor = tokens.onStart)
                        )
                    }

                    HorizontalDivider(color = tokens.border.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 24.dp))
                    SettingsLinkRow(icon = Icons.Default.Shield, label = "Privacy Policy", tint = tokens.hydrationMark, tokens = tokens, onClick = onOpenPrivacyPolicy)
                    HorizontalDivider(color = tokens.border.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 24.dp))
                    SettingsLinkRow(icon = Icons.AutoMirrored.Filled.Logout, label = "Sign Out", tint = tokens.proteinMark, tokens = tokens, onClick = onSignOut)
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun SettingsLinkRow(
    icon: ImageVector,
    label: String,
    tint: Color,
    tokens: com.calovelo.app.presentation.theme.CaloTokens,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
        Text(label, style = MaterialTheme.typography.titleMedium, color = tokens.text, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = tokens.muted.copy(alpha = 0.5f))
    }
}
