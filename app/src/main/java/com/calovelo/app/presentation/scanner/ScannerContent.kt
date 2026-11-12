package com.calovelo.app.presentation.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.calovelo.app.domain.model.FoodItem
import com.calovelo.app.domain.model.ImageSource
import com.calovelo.app.domain.model.MealType
import com.calovelo.app.presentation.theme.CaloTheme
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt

enum class ScannerMode {
    CAMERA,
    MANUAL
}

@Composable
fun ScannerContent(
    modifier: Modifier = Modifier,
    viewModel: FoodScannerViewModel = hiltViewModel()
) {
    val tokens = CaloTheme.tokens
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var scannerMode by remember { mutableStateOf(ScannerMode.CAMERA) }
    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasCameraPermission = granted
        if (!granted) {
            scannerMode = ScannerMode.MANUAL
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.onMessageShown()
        }
    }

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) {
            snackbarHostState.showSnackbar("Meal logged to your journal!")
        }
    }

    val imageCapture = remember { ImageCapture.Builder().build() }
    val preview = remember { Preview.Builder().build() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(tokens.background, tokens.surface.copy(alpha = 0.15f), tokens.background)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Header ──────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "NUTRITION SCANNER",
                        style = MaterialTheme.typography.labelSmall,
                        color = tokens.nutritionMark,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "Log Your Meal",
                        style = MaterialTheme.typography.headlineMedium,
                        color = tokens.text,
                        fontWeight = FontWeight.Black
                    )
                }

                // Plate items counter badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(tokens.nutritionStateContainer ?: tokens.surface.copy(alpha = 0.6f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        "${uiState.items.size} on plate",
                        color = tokens.nutritionMark,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            // ── Segmented Mode Selector ─────────────
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = scannerMode == ScannerMode.CAMERA,
                    onClick = { scannerMode = ScannerMode.CAMERA },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = tokens.aiMark,
                        activeContentColor = tokens.aiInk,
                        inactiveContainerColor = tokens.surface.copy(alpha = 0.5f),
                        inactiveContentColor = tokens.muted
                    )
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("AI Camera", fontWeight = FontWeight.Bold)
                }
                SegmentedButton(
                    selected = scannerMode == ScannerMode.MANUAL,
                    onClick = { scannerMode = ScannerMode.MANUAL },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = tokens.nutritionMark,
                        activeContentColor = Color.White,
                        inactiveContainerColor = tokens.surface.copy(alpha = 0.5f),
                        inactiveContentColor = tokens.muted
                    )
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Manual Search", fontWeight = FontWeight.Bold)
                }
            }

            // ── Content by Mode ─────────────────────
            if (scannerMode == ScannerMode.CAMERA) {
                CameraVisionSection(
                    uiState = uiState,
                    hasCameraPermission = hasCameraPermission,
                    imageCapture = imageCapture,
                    preview = preview,
                    tokens = tokens,
                    context = context,
                    lifecycleOwner = lifecycleOwner,
                    onPhotoReady = viewModel::onPhotoReady,
                    onCaptureFailed = viewModel::onCaptureFailed,
                    onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    onSwitchToManual = { scannerMode = ScannerMode.MANUAL },
                    onRetake = viewModel::onRetake,
                    onScanWithGemini = viewModel::onScanWithGemini,
                    modifier = Modifier.weight(1f)
                )
            } else {
                ManualCatalogSection(
                    uiState = uiState,
                    tokens = tokens,
                    onSearchQueryChanged = viewModel::onSearchQueryChanged,
                    onFoodPicked = viewModel::onFoodPicked,
                    modifier = Modifier.weight(1f)
                )
            }

            // ── The Plate / MealDeck (Visible if items added or in review) ──
            AnimatedVisibility(visible = uiState.items.isNotEmpty()) {
                PlateDeckCard(
                    uiState = uiState,
                    tokens = tokens,
                    onMealTypeSelected = viewModel::onMealTypeSelected,
                    onGramsChanged = viewModel::onGramsChanged,
                    onRemoveItem = viewModel::onRemoveItem,
                    onSave = viewModel::onSave
                )
            }

            Spacer(Modifier.height(60.dp))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 70.dp)
        )
    }
}

// ────────────────────────────────────────────────────────────────────
// Camera Vision Section
// ────────────────────────────────────────────────────────────────────

@Composable
private fun CameraVisionSection(
    uiState: FoodScannerUiState,
    hasCameraPermission: Boolean,
    imageCapture: ImageCapture,
    preview: Preview,
    tokens: com.calovelo.app.presentation.theme.CaloTokens,
    context: android.content.Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    onPhotoReady: (source: ImageSource, previewModel: Any) -> Unit,
    onCaptureFailed: () -> Unit,
    onRequestPermission: () -> Unit,
    onSwitchToManual: () -> Unit,
    onRetake: () -> Unit,
    onScanWithGemini: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(tokens.surface.copy(alpha = 0.5f))
            .border(1.dp, tokens.border.copy(alpha = 0.3f), RoundedCornerShape(26.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (!hasCameraPermission) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = tokens.muted, modifier = Modifier.size(48.dp))
                Text("Camera permission required", color = tokens.text, fontWeight = FontWeight.Bold)
                Text("Allow camera access to scan meals and auto-estimate calories with on-device AI.", textAlign = TextAlign.Center, color = tokens.muted, style = MaterialTheme.typography.bodySmall)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onRequestPermission,
                        colors = ButtonDefaults.buttonColors(containerColor = tokens.aiMark, contentColor = tokens.aiInk)
                    ) {
                        Text("Grant Permission")
                    }
                    OutlinedButton(onClick = onSwitchToManual) {
                        Text("Use Manual Search", color = tokens.text)
                    }
                }
            }
        } else if (uiState.phase == ScannerPhase.ANALYZING) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CircularProgressIndicator(color = tokens.aiMark)
                Text(
                    if (uiState.isGeminiScanning) "Gemini Flash Vision analyzing plate..." else "AI is detecting food & estimating portions...",
                    color = tokens.text,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else if (uiState.phase == ScannerPhase.REVIEW && uiState.capturedImage != null) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = uiState.capturedImage,
                    contentDescription = "Captured Meal",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Top Controls: Retake & Gemini Flash Scan
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onRetake,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = tokens.surface.copy(alpha = 0.85f),
                            contentColor = tokens.text
                        )
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Retake")
                    }

                    Button(
                        onClick = onScanWithGemini,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = tokens.aiMark,
                            contentColor = tokens.aiInk
                        )
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Gemini AI Scan", fontWeight = FontWeight.Bold)
                    }
                }

                if (uiState.status is ScanStatus.NothingDetected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(tokens.surface.copy(alpha = 0.9f))
                            .border(1.dp, tokens.border.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "Local AI confidence low (<60%)",
                                color = tokens.text,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "Tap 'Gemini AI Scan' for precision cloud recognition of everyday staples, or search below.",
                                color = tokens.muted,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        } else if (uiState.status is ScanStatus.ModelUnavailable) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Default.CloudOff, contentDescription = null, tint = tokens.muted, modifier = Modifier.size(40.dp))
                Text("Offline AI Model Initializing", color = tokens.text, fontWeight = FontWeight.Bold)
                Text(uiState.status.reason, color = tokens.muted, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onSwitchToManual,
                        colors = ButtonDefaults.buttonColors(containerColor = tokens.nutritionMark, contentColor = Color.White)
                    ) {
                        Text("Search & Log Manually")
                    }
                    OutlinedButton(onClick = onRetake) {
                        Text("Retry", color = tokens.text)
                    }
                }
            }
        } else {
            // Live Camera Viewfinder
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageCapture
                            )
                            preview.setSurfaceProvider(previewView.surfaceProvider)
                        } catch (_: Exception) {}
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // Viewfinder Target Reticle Frame
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .border(2.dp, tokens.aiMark.copy(alpha = 0.7f), RoundedCornerShape(24.dp))
            )

            // Bottom Capture Action Overlay
            FloatingActionButton(
                onClick = {
                    imageCapture.takePicture(
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageCapturedCallback() {
                            override fun onCaptureSuccess(image: ImageProxy) {
                                val buffer = image.planes[0].buffer
                                val bytes = ByteArray(buffer.remaining())
                                buffer.get(bytes)
                                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                val matrix = Matrix().apply { postRotate(image.imageInfo.rotationDegrees.toFloat()) }
                                val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

                                val stream = ByteArrayOutputStream()
                                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
                                val jpegBytes = stream.toByteArray()

                                onPhotoReady(
                                    ImageSource { ByteArrayInputStream(jpegBytes) },
                                    rotatedBitmap
                                )
                                image.close()
                            }

                            override fun onError(exception: ImageCaptureException) {
                                onCaptureFailed()
                            }
                        }
                    )
                },
                containerColor = tokens.aiMark,
                contentColor = tokens.aiInk,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Capture Food", modifier = Modifier.size(28.dp))
            }
        }
    }
}

// ────────────────────────────────────────────────────────────────────
// Manual Catalog Search Section
// ────────────────────────────────────────────────────────────────────

@Composable
private fun ManualCatalogSection(
    uiState: FoodScannerUiState,
    tokens: com.calovelo.app.presentation.theme.CaloTokens,
    onSearchQueryChanged: (String) -> Unit,
    onFoodPicked: (FoodItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search text field
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = onSearchQueryChanged,
            placeholder = { Text("Search catalog (Dal, Roti, Chicken, Apple...)", color = tokens.muted) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = tokens.muted) },
            trailingIcon = {
                if (uiState.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChanged("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = tokens.muted)
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = tokens.nutritionMark,
                unfocusedBorderColor = tokens.border.copy(alpha = 0.5f),
                focusedTextColor = tokens.text,
                unfocusedTextColor = tokens.text,
                cursorColor = tokens.nutritionMark
            )
        )

        // Catalog List
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(22.dp))
                .background(tokens.surface.copy(alpha = tokens.surfaceAlpha))
                .border(1.dp, tokens.border.copy(alpha = 0.3f), RoundedCornerShape(22.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            if (uiState.searchResults.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No foods found matching query.", color = tokens.muted)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.searchResults, key = { it.labelKey }) { food ->
                        FoodCatalogRow(
                            food = food,
                            tokens = tokens,
                            onAdd = { onFoodPicked(food) }
                        )
                        HorizontalDivider(color = tokens.border.copy(alpha = 0.2f))
                    }
                }
            }
        }
    }
}

@Composable
private fun FoodCatalogRow(
    food: FoodItem,
    tokens: com.calovelo.app.presentation.theme.CaloTokens,
    onAdd: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onAdd)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                food.displayName,
                color = tokens.text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "${food.kcalPer100g.roundToInt()} kcal/100g · P: ${food.proteinPer100g.roundToInt()}g · C: ${food.carbsPer100g.roundToInt()}g · F: ${food.fatPer100g.roundToInt()}g",
                color = tokens.muted,
                style = MaterialTheme.typography.bodySmall
            )
        }

        IconButton(
            onClick = onAdd,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = tokens.nutritionMark.copy(alpha = 0.15f),
                contentColor = tokens.nutritionMark
            ),
            modifier = Modifier.size(36.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add to plate", modifier = Modifier.size(20.dp))
        }
    }
}

// ────────────────────────────────────────────────────────────────────
// Plate Deck / Current Meal Summary Card
// ────────────────────────────────────────────────────────────────────

@Composable
private fun PlateDeckCard(
    uiState: FoodScannerUiState,
    tokens: com.calovelo.app.presentation.theme.CaloTokens,
    onMealTypeSelected: (MealType) -> Unit,
    onGramsChanged: (itemId: Int, grams: Int) -> Unit,
    onRemoveItem: (itemId: Int) -> Unit,
    onSave: () -> Unit
) {
    val totalMacros = uiState.totalMacros

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(tokens.surface.copy(alpha = 0.95f))
            .border(1.dp, tokens.border.copy(alpha = 0.35f), RoundedCornerShape(26.dp))
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Header with total calories
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("CURRENT MEAL", color = tokens.nutritionMark, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                    Text("${totalMacros.kcal.roundToInt()} kcal", color = tokens.text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                }

                // Meal Slot Selector Chips
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    MealType.entries.forEach { type ->
                        val isSelected = uiState.mealType == type
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) tokens.nutritionMark else tokens.border.copy(alpha = 0.3f))
                                .clickable { onMealTypeSelected(type) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                type.name.take(3),
                                color = if (isSelected) Color.White else tokens.muted,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Macros overview row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                MacroPill("Protein", "${totalMacros.proteinG.roundToInt()}g", tokens.proteinMark, tokens)
                MacroPill("Carbs", "${totalMacros.carbsG.roundToInt()}g", tokens.carbsMark, tokens)
                MacroPill("Fat", "${totalMacros.fatG.roundToInt()}g", tokens.fatMark, tokens)
            }

            // Items on plate list (portions)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.food.displayName, color = tokens.text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("${item.macros.kcal.roundToInt()} kcal (${item.grams}g)", color = tokens.muted, style = MaterialTheme.typography.bodySmall)
                        }

                        // Portion adjusters
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedButton(
                                onClick = { onGramsChanged(item.id, (item.grams - 25).coerceAtLeast(10)) },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("-25g", style = MaterialTheme.typography.labelSmall)
                            }
                            OutlinedButton(
                                onClick = { onGramsChanged(item.id, (item.grams + 25).coerceAtMost(1000)) },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("+25g", style = MaterialTheme.typography.labelSmall)
                            }
                            IconButton(onClick = { onRemoveItem(item.id) }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = tokens.muted, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // Dietary Filter Warning
            val conflictingItem = uiState.items.firstOrNull { !it.food.matchesFilter(uiState.dietaryFilter) }
            if (conflictingItem != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF3E2723))
                        .border(1.dp, Color(0xFFFF8A65).copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFF8A65), modifier = Modifier.size(20.dp))
                        Text(
                            "Dietary Warning: ${conflictingItem.food.displayName} contains ingredients conflicting with your active ${uiState.dietaryFilter.displayName} diet filter.",
                            color = Color(0xFFFFCCBC),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Log Meal Primary Button
            Button(
                onClick = onSave,
                enabled = !uiState.isSaving && uiState.items.isNotEmpty(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = tokens.nutritionMark, contentColor = Color.White),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                } else {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Log Meal to Journal", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
private fun MacroPill(label: String, value: String, accent: Color, tokens: com.calovelo.app.presentation.theme.CaloTokens) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = tokens.background.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, tokens.border.copy(alpha = 0.25f)),
        modifier = Modifier.width(90.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(accent))
            Spacer(Modifier.height(4.dp))
            Text(value, color = tokens.text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black)
            Text(label, color = tokens.muted, style = MaterialTheme.typography.labelSmall)
        }
    }
}
