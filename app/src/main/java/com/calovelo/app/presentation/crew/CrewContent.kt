package com.calovelo.app.presentation.crew

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calovelo.app.presentation.theme.CaloTheme

@Composable
fun CrewContent(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {}
) {
    val tokens = CaloTheme.tokens
    var rsvpJoined by remember { mutableStateOf(false) }
    var kudosCount by remember { mutableIntStateOf(24) }
    var hasGivenKudos by remember { mutableStateOf(false) }

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
            // ── Header with Back ────────────────────
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
                            "CREW & CLUB",
                            style = MaterialTheme.typography.labelSmall,
                            color = tokens.nutritionMark,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            "Sunset Striders",
                            style = MaterialTheme.typography.headlineMedium,
                            color = tokens.text,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background((tokens.aiStateContainer ?: tokens.surface).copy(alpha = 0.6f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("142 Members", color = tokens.aiMark, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            // ── Monthly Club Challenge Progress Card ─
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(tokens.surface.copy(alpha = tokens.surfaceAlpha))
                        .border(1.dp, tokens.border.copy(alpha = 0.3f), RoundedCornerShape(28.dp))
                        .padding(24.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Monthly Distance Challenge", style = MaterialTheme.typography.titleMedium, color = tokens.text, fontWeight = FontWeight.Bold)
                            Text("74%", style = MaterialTheme.typography.titleMedium, color = tokens.energyMark, fontWeight = FontWeight.Black)
                        }

                        LinearProgressIndicator(
                            progress = { 0.74f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = tokens.energyMark,
                            trackColor = tokens.border.copy(alpha = 0.4f),
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("1,840 km completed", style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                            Text("Target: 2,500 km", style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                        }
                    }
                }
            }

            // ── Upcoming Group Event Card ───────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(26.dp))
                        .background(tokens.surface.copy(alpha = tokens.surfaceAlpha))
                        .border(1.dp, tokens.border.copy(alpha = 0.3f), RoundedCornerShape(26.dp))
                        .padding(22.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(tokens.nutritionMark.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Event, contentDescription = null, tint = tokens.nutritionMark, modifier = Modifier.size(20.dp))
                                }
                                Text("Friday Sunset Ride", style = MaterialTheme.typography.titleMedium, color = tokens.text, fontWeight = FontWeight.Bold)
                            }
                            Text("Friday, 6:00 PM", style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                        }

                        Text(
                            "Meet at Central Park Gate 4 for a 35 km scenic loop with social coffee & recovery stretch at the finish.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = tokens.muted
                        )

                        Button(
                            onClick = { rsvpJoined = !rsvpJoined },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (rsvpJoined) tokens.greenStateContainer ?: tokens.energyMark.copy(alpha = 0.3f) else tokens.nutritionMark,
                                contentColor = if (rsvpJoined) tokens.energyMark else Color.White
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Icon(if (rsvpJoined) Icons.Default.Check else Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (rsvpJoined) "Attending (19 Athletes)" else "RSVP to Join Ride (18 Attending)",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // ── Community Activity Feed Card ────────
            item {
                Text(
                    "COMMUNITY FEED",
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.muted,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(26.dp))
                        .background(tokens.surface.copy(alpha = tokens.surfaceAlpha))
                        .border(1.dp, tokens.border.copy(alpha = 0.3f), RoundedCornerShape(26.dp))
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(tokens.startFill.copy(alpha = 0.15f))
                                    .border(1.dp, tokens.startFill.copy(alpha = 0.4f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = tokens.startFill, modifier = Modifier.size(24.dp))
                            }
                            Column {
                                Text("Alex Rivera", style = MaterialTheme.typography.titleMedium, color = tokens.text, fontWeight = FontWeight.Bold)
                                Text("2 hours ago · Morning Gran Fondo (38.4 km)", style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                            }
                        }

                        Text(
                            "Incredible sunrise views over the ridge today! Pushed an avg 32 km/h tempo pace. Hydration on point.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = tokens.text
                        )

                        // Route / Photo container
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(tokens.mapBackground)
                                .border(1.dp, tokens.border.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Map, contentDescription = null, tint = tokens.energyMark, modifier = Modifier.size(36.dp))
                                Text("38.4 km · +420m Elevation · 1:12:30", color = tokens.muted, style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    if (!hasGivenKudos) {
                                        kudosCount++
                                        hasGivenKudos = true
                                    } else {
                                        kudosCount--
                                        hasGivenKudos = false
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = if (hasGivenKudos) tokens.heartMark else tokens.muted,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("$kudosCount Kudos", color = if (hasGivenKudos) tokens.heartMark else tokens.text, fontWeight = FontWeight.Bold)
                            }

                            TextButton(onClick = {}) {
                                Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = tokens.muted, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("5 Comments", color = tokens.muted)
                            }
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
