package com.calovelo.app.presentation.insights

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calovelo.app.domain.model.MealType
import com.calovelo.app.presentation.theme.CaloTheme

@Composable
fun InsightsContent(
    modifier: Modifier = Modifier,
    viewModel: NutritionViewModel = hiltViewModel()
) {
    val tokens = CaloTheme.tokens
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.userMessage) {
        state.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.onMessageShown()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        tokens.background,
                        tokens.surface.copy(alpha = 0.15f),
                        tokens.background
                    )
                )
            )
    ) {
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = tokens.nutritionMark)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 28.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // ── Header ──────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "NUTRITION",
                            style = MaterialTheme.typography.labelSmall,
                            color = tokens.nutritionMark,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "Today's Macros",
                            style = MaterialTheme.typography.headlineMedium,
                            color = tokens.text,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                (tokens.nutritionStateContainer ?: tokens.surface)
                                    .copy(alpha = 0.6f)
                            )
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            "${state.remainingKcal} kcal left",
                            color = tokens.nutritionMark,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                // ── Macro Rings Card ────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(tokens.surface.copy(alpha = tokens.surfaceAlpha))
                        .border(
                            1.dp,
                            tokens.border.copy(alpha = 0.3f),
                            RoundedCornerShape(28.dp)
                        )
                        .padding(24.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        // Calorie hero ring
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val calProgress by animateFloatAsState(
                                targetValue = state.calories.progress,
                                animationSpec = tween(1000),
                                label = "calRing"
                            )
                            Canvas(modifier = Modifier.size(140.dp)) {
                                val strokeW = 14.dp.toPx()
                                drawArc(
                                    tokens.border.copy(alpha = 0.3f),
                                    0f, 360f, false,
                                    style = Stroke(strokeW, cap = StrokeCap.Round)
                                )
                                drawArc(
                                    color = if (state.calories.isOverGoal) tokens.proteinMark else tokens.energyMark,
                                    startAngle = -90f,
                                    sweepAngle = 360f * calProgress,
                                    useCenter = false,
                                    style = Stroke(strokeW, cap = StrokeCap.Round)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "${state.calories.consumed}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = tokens.text,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    "of ${state.calories.goal} kcal",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = tokens.muted
                                )
                            }
                        }

                        // P / C / F mini rings row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            MacroMiniRing(
                                ring = state.protein,
                                accent = tokens.proteinMark,
                                tokens = tokens
                            )
                            MacroMiniRing(
                                ring = state.carbs,
                                accent = tokens.carbsMark,
                                tokens = tokens
                            )
                            MacroMiniRing(
                                ring = state.fat,
                                accent = tokens.fatMark,
                                tokens = tokens
                            )
                        }
                    }
                }

                // ── AI Plan Generation ──────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            (tokens.aiStateContainer ?: tokens.surface)
                                .copy(alpha = 0.7f)
                        )
                        .border(
                            1.dp,
                            tokens.border.copy(alpha = 0.3f),
                            RoundedCornerShape(24.dp)
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = tokens.aiMark,
                            modifier = Modifier.size(28.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "AI Meal Planner",
                                style = MaterialTheme.typography.titleMedium,
                                color = tokens.text,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                if (state.plannedKcal > 0) "${state.plannedKcal} kcal planned"
                                else "Generate a plan for remaining meals",
                                style = MaterialTheme.typography.bodySmall,
                                color = tokens.muted
                            )
                        }
                        Button(
                            onClick = viewModel::onGeneratePlan,
                            enabled = !state.isGenerating,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = tokens.aiMark,
                                contentColor = tokens.aiInk
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            if (state.isGenerating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = tokens.aiInk
                                )
                            } else {
                                Text("Plan", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // ── Planned Meals (if any) ──────────────────
                if (state.plannedSections.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "PLANNED MEALS",
                                style = MaterialTheme.typography.labelSmall,
                                color = tokens.aiMark,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            TextButton(onClick = viewModel::onClearPlan) {
                                Text("Clear", color = tokens.muted)
                            }
                        }

                        state.plannedSections.forEach { section ->
                            MealSectionCard(
                                section = section,
                                isPlanned = true,
                                tokens = tokens,
                                onMarkEaten = viewModel::onMarkEaten,
                                onDelete = viewModel::onDeleteMeal
                            )
                        }
                    }
                }

                // ── Logged Meals Journal ────────────────────
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "TODAY'S MEAL JOURNAL",
                        style = MaterialTheme.typography.labelSmall,
                        color = tokens.muted,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )

                    val nonEmpty = state.loggedSections.filter { it.rows.isNotEmpty() }
                    if (nonEmpty.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(tokens.surface.copy(alpha = 0.4f))
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Restaurant,
                                    contentDescription = null,
                                    tint = tokens.muted.copy(alpha = 0.5f),
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "No meals logged yet",
                                    color = tokens.muted,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    "Use the Scanner tab to snap a meal",
                                    color = tokens.muted.copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    } else {
                        nonEmpty.forEach { section ->
                            MealSectionCard(
                                section = section,
                                isPlanned = false,
                                tokens = tokens,
                                onMarkEaten = {},
                                onDelete = viewModel::onDeleteMeal
                            )
                        }
                    }
                }

                Spacer(Modifier.height(80.dp))
            }
        }

        // Snackbar host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ────────────────────────────────────────────────────────────────────
// Reusable Sub-composables
// ────────────────────────────────────────────────────────────────────

@Composable
private fun MacroMiniRing(
    ring: MacroRingUi,
    accent: Color,
    tokens: com.calovelo.app.presentation.theme.CaloTokens
) {
    val progress by animateFloatAsState(
        targetValue = ring.progress,
        animationSpec = tween(900),
        label = ring.label
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(64.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val sw = 6.dp.toPx()
                drawArc(
                    tokens.border.copy(alpha = 0.25f),
                    0f, 360f, false,
                    style = Stroke(sw, cap = StrokeCap.Round)
                )
                drawArc(
                    color = if (ring.isOverGoal) tokens.proteinMark else accent,
                    -90f, 360f * progress, false,
                    style = Stroke(sw, cap = StrokeCap.Round)
                )
            }
            Text(
                "${ring.consumed}",
                style = MaterialTheme.typography.labelLarge,
                color = tokens.text,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(ring.label, style = MaterialTheme.typography.labelSmall, color = tokens.muted)
        Text(
            "/ ${ring.goal}${ring.unit}",
            style = MaterialTheme.typography.labelSmall,
            color = tokens.muted.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun MealSectionCard(
    section: MealSectionUi,
    isPlanned: Boolean,
    tokens: com.calovelo.app.presentation.theme.CaloTokens,
    onMarkEaten: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    val mealIcon = when (section.mealType) {
        MealType.BREAKFAST -> Icons.Default.WbSunny
        MealType.LUNCH -> Icons.Default.LunchDining
        MealType.DINNER -> Icons.Default.DinnerDining
        MealType.SNACK -> Icons.Default.Icecream
    }
    val accentColor = if (isPlanned) tokens.aiMark else tokens.nutritionMark

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(tokens.surface.copy(alpha = tokens.surfaceAlpha))
            .border(1.dp, tokens.border.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(mealIcon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(
                            section.mealType.name.lowercase()
                                .replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.titleMedium,
                            color = tokens.text,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${section.kcal} kcal",
                            style = MaterialTheme.typography.labelSmall,
                            color = accentColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                if (isPlanned) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(accentColor.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "Planned",
                            style = MaterialTheme.typography.labelSmall,
                            color = accentColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            section.rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            row.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = tokens.text,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "${row.grams}g · ${row.kcal} kcal · P:${row.proteinG}g",
                            style = MaterialTheme.typography.bodySmall,
                            color = tokens.muted
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (isPlanned) {
                            IconButton(
                                onClick = { onMarkEaten(row.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.CheckCircleOutline,
                                    contentDescription = "Mark eaten",
                                    tint = tokens.energyMark,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        IconButton(
                            onClick = { onDelete(row.id) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove",
                                tint = tokens.muted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
