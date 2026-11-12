package com.calovelo.app.presentation.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.sp
import com.calovelo.app.domain.model.SportActivity
import com.calovelo.app.domain.model.SportCategory
import com.calovelo.app.domain.model.StravaSportsCatalog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SportSelectorBottomSheet(
    selectedSport: SportActivity,
    onSportSelected: (SportActivity) -> Unit,
    onDismissRequest: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val stravaOrange = Color(0xFFFC4C02)
    val darkSurface = Color(0xFF141414)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = darkSurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(44.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.Gray.copy(alpha = 0.5f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                // "Your Top Sports" Section
                item {
                    Text(
                        text = "Your Top Sports",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Spacer(Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TopSportIconItem(
                            sport = StravaSportsCatalog.RUN,
                            isSelected = selectedSport.id == StravaSportsCatalog.RUN.id,
                            icon = Icons.Default.DirectionsRun,
                            stravaOrange = stravaOrange,
                            onClick = {
                                onSportSelected(StravaSportsCatalog.RUN)
                                onDismissRequest()
                            }
                        )
                        TopSportIconItem(
                            sport = StravaSportsCatalog.RIDE,
                            isSelected = selectedSport.id == StravaSportsCatalog.RIDE.id,
                            icon = Icons.Default.DirectionsBike,
                            stravaOrange = stravaOrange,
                            onClick = {
                                onSportSelected(StravaSportsCatalog.RIDE)
                                onDismissRequest()
                            }
                        )
                        TopSportIconItem(
                            sport = StravaSportsCatalog.WEIGHT_TRAINING,
                            isSelected = selectedSport.id == StravaSportsCatalog.WEIGHT_TRAINING.id,
                            icon = Icons.Default.FitnessCenter,
                            stravaOrange = stravaOrange,
                            onClick = {
                                onSportSelected(StravaSportsCatalog.WEIGHT_TRAINING)
                                onDismissRequest()
                            }
                        )
                    }

                    Spacer(Modifier.height(28.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    Spacer(Modifier.height(20.dp))
                }

                // Categorized Sports Sections
                val categories = listOf(
                    SportCategory.FOOT,
                    SportCategory.CYCLE,
                    SportCategory.STRENGTH,
                    SportCategory.RACKET,
                    SportCategory.WATER,
                    SportCategory.WINTER,
                    SportCategory.TEAM,
                    SportCategory.OTHER
                )

                categories.forEach { category ->
                    val sportsInCat = StravaSportsCatalog.allSports.filter { it.category == category }
                    if (sportsInCat.isNotEmpty()) {
                        item(key = "header_${category.name}") {
                            Text(
                                text = category.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }

                        items(sportsInCat, key = { it.id }) { sport ->
                            val isSelected = selectedSport.id == sport.id
                            SportRowItem(
                                sport = sport,
                                isSelected = isSelected,
                                stravaOrange = stravaOrange,
                                onClick = {
                                    onSportSelected(sport)
                                    onDismissRequest()
                                }
                            )
                        }

                        item(key = "divider_${category.name}") {
                            Spacer(Modifier.height(14.dp))
                            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                            Spacer(Modifier.height(14.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TopSportIconItem(
    sport: SportActivity,
    isSelected: Boolean,
    icon: ImageVector,
    stravaOrange: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(if (isSelected) stravaOrange.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.08f))
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) stravaOrange else Color.White.copy(alpha = 0.15f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = sport.displayName,
                tint = if (isSelected) stravaOrange else Color.White,
                modifier = Modifier.size(28.dp)
            )

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(stravaOrange),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.Black,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        Text(
            text = sport.displayName,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
            color = if (isSelected) stravaOrange else Color.White
        )
    }
}

@Composable
private fun SportRowItem(
    sport: SportActivity,
    isSelected: Boolean,
    stravaOrange: Color,
    onClick: () -> Unit
) {
    val icon = when (sport.category) {
        SportCategory.FOOT -> Icons.Default.DirectionsRun
        SportCategory.CYCLE -> Icons.Default.DirectionsBike
        SportCategory.STRENGTH -> Icons.Default.FitnessCenter
        SportCategory.WATER -> Icons.Default.Pool
        SportCategory.WINTER -> Icons.Default.DownhillSkiing
        SportCategory.TEAM -> Icons.Default.SportsSoccer
        else -> Icons.Default.SportsHandball
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) stravaOrange else Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = sport.displayName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                color = if (isSelected) stravaOrange else Color.White
            )
        }

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = stravaOrange,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
