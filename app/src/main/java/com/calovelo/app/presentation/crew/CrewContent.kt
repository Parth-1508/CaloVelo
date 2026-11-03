package com.calovelo.app.presentation.crew

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.calovelo.app.presentation.theme.CaloTheme

@Composable
fun CrewContent(
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
                    Text("CREW & CLUB", style = MaterialTheme.typography.labelMedium, color = tokens.muted, fontWeight = FontWeight.Bold)
                    Text("Sunset Striders", style = MaterialTheme.typography.headlineMedium, color = tokens.text, fontWeight = FontWeight.Bold)
                }
                Badge(containerColor = tokens.aiStateContainer ?: tokens.surface) {
                    Text("142 Members", color = tokens.aiMark, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                }
            }

            // Monthly Club Challenge Progress Bar
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("October Distance Challenge", style = MaterialTheme.typography.titleMedium, color = tokens.text, fontWeight = FontWeight.Bold)
                        Text("1,840 / 2,500 km", style = MaterialTheme.typography.bodyMedium, color = tokens.energyMark, fontWeight = FontWeight.Bold)
                    }
                    LinearProgressIndicator(
                        progress = { 0.74f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = tokens.energyMark,
                        trackColor = tokens.border,
                    )
                }
            }

            // Sunset Striders Event Card
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Event, contentDescription = null, tint = tokens.nutritionMark)
                            Text("Sunset Friday Group Ride", style = MaterialTheme.typography.titleMedium, color = tokens.text, fontWeight = FontWeight.Bold)
                        }
                        Text("Friday, 6:00 PM", style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                    }
                    Text("Meet at Central Park Gate 4 for a 35km scenic loop with coffee stop.", style = MaterialTheme.typography.bodyMedium, color = tokens.muted)
                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(containerColor = tokens.nutritionMark, contentColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("RSVP Attending (18 Joined)")
                    }
                }
            }

            // Activity Post with Photo / Kudos
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(shape = CircleShape, color = tokens.border, modifier = Modifier.size(40.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = tokens.text)
                            }
                        }
                        Column {
                            Text("Alex Rivera", style = MaterialTheme.typography.titleSmall, color = tokens.text, fontWeight = FontWeight.Bold)
                            Text("2 hours ago · Morning Gran Fondo", style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                        }
                    }

                    Text("Incredible sunrise views over the ridge today! Pushed a solid 32 km/h average.", style = MaterialTheme.typography.bodyMedium, color = tokens.text)

                    // Photo placeholder
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = tokens.mapBackground,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Image, contentDescription = null, tint = tokens.muted, modifier = Modifier.size(48.dp))
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = {}) {
                            Icon(Icons.Default.Favorite, contentDescription = null, tint = tokens.heartMark)
                            Spacer(Modifier.width(6.dp))
                            Text("24 Kudos", color = tokens.text)
                        }
                        TextButton(onClick = {}) {
                            Icon(Icons.Default.Comment, contentDescription = null, tint = tokens.muted)
                            Spacer(Modifier.width(6.dp))
                            Text("5 Comments", color = tokens.muted)
                        }
                    }
                }
            }
        }
    }
}
