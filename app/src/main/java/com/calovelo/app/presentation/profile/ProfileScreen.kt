package com.calovelo.app.presentation.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.calovelo.app.domain.model.WorkoutSession
import com.calovelo.app.presentation.theme.CaloTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private enum class ProfileTab(val title: String) {
    PROGRESS("Progress"),
    WORKOUTS("Workouts"),
    ACTIVITIES("Activities")
}

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val tokens = CaloTheme.tokens
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(ProfileTab.PROGRESS) }
    var showEditDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.onAvatarSelected(uri)
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onDismissMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = tokens.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(tokens.background)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // Top Strava-style App Bar
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            IconButton(
                                onClick = onNavigateBack,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(tokens.surface.copy(alpha = 0.5f))
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = tokens.text
                                )
                            }
                            Text(
                                text = "You",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Black,
                                color = tokens.text
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Mini avatar thumbnail on top right
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, tokens.energyMark, CircleShape)
                                    .clickable {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (!state.avatarUri.isNullOrBlank()) {
                                    AsyncImage(
                                        model = state.avatarUri,
                                        contentDescription = "User Avatar",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = tokens.energyMark,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Athlete Hero Section (Avatar, Name, Location, Bio, Edit Button)
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Large Avatar with Change Photo badge
                        Box(
                            modifier = Modifier
                                .size(104.dp)
                                .clip(CircleShape)
                                .border(2.5.dp, tokens.energyMark, CircleShape)
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (!state.avatarUri.isNullOrBlank()) {
                                AsyncImage(
                                    model = state.avatarUri,
                                    contentDescription = "Profile Photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(tokens.surface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = tokens.energyMark,
                                        modifier = Modifier.size(54.dp)
                                    )
                                }
                            }

                            // Camera overlay icon
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(tokens.energyMark)
                                    .border(1.5.dp, tokens.background, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = "Change photo",
                                    tint = tokens.background,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        Text(
                            text = state.userName,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = tokens.text
                        )

                        if (state.locationString.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = tokens.muted,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = state.locationString,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = tokens.muted
                                )
                            }
                        }

                        if (state.bioText.isNotBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = state.bioText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = tokens.text.copy(alpha = 0.85f)
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // Edit Profile & Photo buttons row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showEditDialog = true },
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, tokens.border.copy(alpha = 0.5f)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = tokens.surface.copy(alpha = 0.3f),
                                    contentColor = tokens.text
                                ),
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Edit Profile", fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = tokens.energyMark,
                                    contentColor = tokens.background
                                ),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Photo", fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(24.dp)) }

                // Strava-style Tab Row (Progress | Workouts | Activities)
                item {
                    TabRow(
                        selectedTabIndex = selectedTab.ordinal,
                        containerColor = tokens.background,
                        contentColor = tokens.text,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                                color = tokens.energyMark,
                                height = 3.dp
                            )
                        },
                        divider = {
                            HorizontalDivider(color = tokens.border.copy(alpha = 0.2f))
                        }
                    ) {
                        ProfileTab.entries.forEach { tab ->
                            val isSelected = selectedTab == tab
                            Tab(
                                selected = isSelected,
                                onClick = { selectedTab = tab },
                                text = {
                                    Text(
                                        text = tab.title,
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                        color = if (isSelected) tokens.text else tokens.muted,
                                        fontSize = 15.sp
                                    )
                                }
                            )
                        }
                    }
                }

                // Tab Content
                when (selectedTab) {
                    ProfileTab.PROGRESS -> {
                        item {
                            ProgressTabContent(
                                state = state,
                                tokens = tokens
                            )
                        }
                    }
                    ProfileTab.WORKOUTS -> {
                        if (state.workouts.isEmpty()) {
                            item {
                                EmptyStateCard(
                                    message = "No workouts recorded yet.\nStart a run or lift session to see your history!",
                                    tokens = tokens
                                )
                            }
                        } else {
                            items(state.workouts) { workout ->
                                WorkoutListItem(workout = workout, tokens = tokens)
                            }
                        }
                    }
                    ProfileTab.ACTIVITIES -> {
                        if (state.workouts.isEmpty()) {
                            item {
                                EmptyStateCard(
                                    message = "No activity posts yet.\nRecorded activities will appear in your Strava feed!",
                                    tokens = tokens
                                )
                            }
                        } else {
                            items(state.workouts) { workout ->
                                ActivityFeedCard(
                                    workout = workout,
                                    userName = state.userName,
                                    avatarUri = state.avatarUri,
                                    location = state.locationString,
                                    tokens = tokens
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        EditProfileDialog(
            state = state,
            onDismiss = { showEditDialog = false },
            onSave = { name, age, height, weight, targetWeight, bio, location ->
                viewModel.updateProfile(name, age, height, weight, targetWeight, bio, location)
                showEditDialog = false
            }
        )
    }
}

@Composable
private fun ProgressTabContent(
    state: ProfileUiState,
    tokens: com.calovelo.app.presentation.theme.CaloTokens
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Weekly Streak & Highlights Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            tokens.energyMark.copy(alpha = 0.25f),
                            tokens.surface.copy(alpha = 0.5f)
                        )
                    )
                )
                .border(1.dp, tokens.energyMark.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "WEEKLY STREAK",
                        style = MaterialTheme.typography.labelSmall,
                        color = tokens.energyMark,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        "${state.weeklyStreak} WEEKS",
                        style = MaterialTheme.typography.headlineMedium,
                        color = tokens.text,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        "Consistent athlete. Keep pushing!",
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.muted
                    )
                }

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(tokens.energyMark.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = tokens.energyMark,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        // Weekly Activity Numbers
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(tokens.surface.copy(alpha = 0.4f))
                .border(1.dp, tokens.border.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "THIS WEEK'S PROGRESS",
                    style = MaterialTheme.typography.labelMedium,
                    color = tokens.muted,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatMetricColumn(
                        label = "Distance",
                        value = String.format(Locale.US, "%.1f km", state.weeklyDistanceKm),
                        tokens = tokens
                    )
                    StatMetricColumn(
                        label = "Time",
                        value = formatDurationHoursMinutes(state.weeklyTimeSeconds),
                        tokens = tokens
                    )
                    StatMetricColumn(
                        label = "Activities",
                        value = state.totalActivitiesCount.toString(),
                        tokens = tokens
                    )
                }
            }
        }

        // Biometrics & Body Composition Card with BMI
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(tokens.surface.copy(alpha = 0.4f))
                .border(1.dp, tokens.border.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "BIOMETRICS & BMI",
                        style = MaterialTheme.typography.labelMedium,
                        color = tokens.muted,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = state.dietaryFilter.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = tokens.energyMark,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatMetricColumn(label = "Age", value = "${state.ageYears} yrs", tokens = tokens)
                    StatMetricColumn(label = "Height", value = "${state.heightCm} cm", tokens = tokens)
                    StatMetricColumn(label = "Weight", value = String.format(Locale.US, "%.1f kg", state.weightKg), tokens = tokens)
                    StatMetricColumn(label = "Target", value = String.format(Locale.US, "%.1f kg", state.targetWeightKg), tokens = tokens)
                }

                HorizontalDivider(color = tokens.border.copy(alpha = 0.2f))

                // BMI Display & Health Indicator
                val bmiCategory = when {
                    state.bmi < 18.5f -> "Underweight" to Color(0xFF64B5F6)
                    state.bmi < 25.0f -> "Normal / Healthy Weight" to Color(0xFF81C784)
                    state.bmi < 30.0f -> "Overweight" to Color(0xFFFFB74D)
                    else -> "Obese" to Color(0xFFE57373)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "BMI: ${String.format(Locale.US, "%.1f", state.bmi)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = tokens.text
                        )
                        Text(
                            bmiCategory.first,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = bmiCategory.second
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(bmiCategory.second.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "Active Target",
                            style = MaterialTheme.typography.labelSmall,
                            color = bmiCategory.second,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        // Achievements & PRs Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(tokens.surface.copy(alpha = 0.4f))
                .border(1.dp, tokens.border.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "PERSONAL RECORDS & TROPHIES",
                        style = MaterialTheme.typography.labelMedium,
                        color = tokens.muted,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PrBadgeItem(title = "Fastest 1 km", record = "4:12", tokens = tokens, modifier = Modifier.weight(1f))
                    PrBadgeItem(title = "Fastest 5 km", record = "22:45", tokens = tokens, modifier = Modifier.weight(1f))
                    PrBadgeItem(title = "Longest Run", record = "12.4 km", tokens = tokens, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun StatMetricColumn(
    label: String,
    value: String,
    tokens: com.calovelo.app.presentation.theme.CaloTokens
) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = tokens.text)
        Text(label, style = MaterialTheme.typography.bodySmall, color = tokens.muted)
    }
}

@Composable
private fun PrBadgeItem(
    title: String,
    record: String,
    tokens: com.calovelo.app.presentation.theme.CaloTokens,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(tokens.surface.copy(alpha = 0.5f))
            .border(1.dp, tokens.border.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(record, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = tokens.energyMark)
            Text(title, style = MaterialTheme.typography.labelSmall, color = tokens.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun WorkoutListItem(
    workout: WorkoutSession,
    tokens: com.calovelo.app.presentation.theme.CaloTokens
) {
    val dateStr = remember(workout.startTime) {
        Instant.ofEpochMilli(workout.startTime)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("MMM dd, yyyy · h:mm a"))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(tokens.surface.copy(alpha = 0.35f))
            .border(1.dp, tokens.border.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(tokens.energyMark.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (workout.distanceMeters > 0) Icons.Default.DirectionsRun else Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = tokens.energyMark,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        workout.activityType.replace("_", " ").replaceFirstChar { it.uppercase() }.ifBlank { "Workout Session" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = tokens.text
                    )
                    Text(
                        dateStr,
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.muted
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                if (workout.distanceMeters > 0) {
                    Text(
                        String.format(Locale.US, "%.2f km", workout.distanceMeters / 1000.0),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = tokens.text
                    )
                }
                Text(
                    formatDurationHoursMinutes(workout.durationSeconds),
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.muted
                )
            }
        }
    }
}

@Composable
private fun ActivityFeedCard(
    workout: WorkoutSession,
    userName: String,
    avatarUri: String?,
    location: String,
    tokens: com.calovelo.app.presentation.theme.CaloTokens
) {
    val dateStr = remember(workout.startTime) {
        Instant.ofEpochMilli(workout.startTime)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a"))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(tokens.surface.copy(alpha = 0.4f))
            .border(1.dp, tokens.border.copy(alpha = 0.25f), RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Header: Avatar, Name, Timestamp, Location
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .border(1.dp, tokens.energyMark, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (!avatarUri.isNullOrBlank()) {
                        AsyncImage(
                            model = avatarUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.Person, contentDescription = null, tint = tokens.energyMark, modifier = Modifier.size(24.dp))
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        userName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = tokens.text
                    )
                    Text(
                        "$dateStr · CaloVelo",
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.muted
                    )
                    if (location.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = tokens.muted, modifier = Modifier.size(12.dp))
                            Text(location, style = MaterialTheme.typography.labelSmall, color = tokens.muted)
                        }
                    }
                }
            }

            // Title & User Note
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = workout.activityType.replace("_", " ").replaceFirstChar { it.uppercase() }.ifBlank { "Morning Session" },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = tokens.text
                )
                val notes = workout.userNotes.orEmpty()
                if (notes.isNotBlank()) {
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = tokens.text.copy(alpha = 0.85f)
                    )
                }
            }

            // Key Stats: Distance, Pace, Achievements
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (workout.distanceMeters > 0) {
                    StatMetricColumn(
                        label = "Distance",
                        value = String.format(Locale.US, "%.2f km", workout.distanceMeters / 1000.0),
                        tokens = tokens
                    )
                    val paceMinPerKm = if (workout.distanceMeters > 0 && workout.durationSeconds > 0) {
                        (workout.durationSeconds.toDouble() / 60.0) / (workout.distanceMeters / 1000.0)
                    } else 0.0
                    val paceMin = paceMinPerKm.toInt()
                    val paceSec = ((paceMinPerKm - paceMin) * 60).toInt()
                    StatMetricColumn(
                        label = "Pace",
                        value = String.format(Locale.US, "%d:%02d /km", paceMin, paceSec),
                        tokens = tokens
                    )
                } else {
                    StatMetricColumn(
                        label = "Duration",
                        value = formatDurationHoursMinutes(workout.durationSeconds),
                        tokens = tokens
                    )
                    StatMetricColumn(
                        label = "Calories",
                        value = "${workout.caloriesBurned} kcal",
                        tokens = tokens
                    )
                }

                // Achievements Badge
                Column(horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Achievements", style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(18.dp))
                        Text("3", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = tokens.text)
                    }
                }
            }

            // PR Banner (Strava style)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2E2415))
                    .border(1.dp, Color(0xFFDDAA33).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFFFB300)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("PR", fontWeight = FontWeight.Black, fontSize = 11.sp, color = Color.Black)
                    }
                    Text(
                        "Personal Record · High Intensity Effort!",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFE082)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyStateCard(
    message: String,
    tokens: com.calovelo.app.presentation.theme.CaloTokens
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(tokens.surface.copy(alpha = 0.3f))
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = tokens.muted,
            lineHeight = 22.sp
        )
    }
}

@Composable
private fun EditProfileDialog(
    state: ProfileUiState,
    onDismiss: () -> Unit,
    onSave: (name: String, age: Int, height: Int, weight: Float, targetWeight: Float, bio: String, location: String) -> Unit
) {
    val tokens = CaloTheme.tokens
    var name by remember { mutableStateOf(state.userName) }
    var ageStr by remember { mutableStateOf(state.ageYears.toString()) }
    var heightStr by remember { mutableStateOf(state.heightCm.toString()) }
    var weightStr by remember { mutableStateOf(state.weightKg.toString()) }
    var targetWeightStr by remember { mutableStateOf(state.targetWeightKg.toString()) }
    var bio by remember { mutableStateOf(state.bioText) }
    var location by remember { mutableStateOf(state.locationString) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Edit Athlete Profile", fontWeight = FontWeight.Black, color = tokens.text)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Display Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = ageStr,
                        onValueChange = { ageStr = it },
                        label = { Text("Age") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = heightStr,
                        onValueChange = { heightStr = it },
                        label = { Text("Height (cm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = weightStr,
                        onValueChange = { weightStr = it },
                        label = { Text("Weight (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = targetWeightStr,
                        onValueChange = { targetWeightStr = it },
                        label = { Text("Target (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val age = ageStr.toIntOrNull() ?: state.ageYears
                    val height = heightStr.toIntOrNull() ?: state.heightCm
                    val weight = weightStr.toFloatOrNull() ?: state.weightKg
                    val targetWeight = targetWeightStr.toFloatOrNull() ?: state.targetWeightKg
                    onSave(name, age, height, weight, targetWeight, bio, location)
                },
                colors = ButtonDefaults.buttonColors(containerColor = tokens.energyMark, contentColor = tokens.background)
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = tokens.muted)
            }
        },
        containerColor = tokens.surface,
        shape = RoundedCornerShape(24.dp)
    )
}

private fun formatDurationHoursMinutes(seconds: Long): String {
    val hrs = seconds / 3600
    val mins = (seconds % 3600) / 60
    return if (hrs > 0) "${hrs}h ${mins}m" else "${mins}m"
}
