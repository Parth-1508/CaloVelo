package com.calovelo.app.presentation.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.Image
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calovelo.app.domain.model.ImageSource
import com.calovelo.app.presentation.theme.CaloTheme
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

@Composable
fun ScannerContent(
    modifier: Modifier = Modifier,
    viewModel: FoodScannerViewModel = hiltViewModel()
) {
    val tokens = CaloTheme.tokens
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val imageCapture = remember { ImageCapture.Builder().build() }
    val preview = remember { Preview.Builder().build() }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = tokens.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                    Text("AI VISION SCANNER", style = MaterialTheme.typography.labelMedium, color = tokens.muted, fontWeight = FontWeight.Bold)
                    Text("Meal & Macro Detection", style = MaterialTheme.typography.headlineMedium, color = tokens.text, fontWeight = FontWeight.Bold)
                }
                Badge(containerColor = tokens.aiStateContainer ?: tokens.surface) {
                    Text(
                        text = when (uiState.phase) {
                            ScannerPhase.LIVE -> "AI Ready"
                            ScannerPhase.ANALYZING -> "Analyzing..."
                            ScannerPhase.REVIEW -> "Reviewing"
                        },
                        color = tokens.aiMark,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            // Viewfinder / Captured Image Display
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = tokens.surface,
                border = BorderStroke(1.dp, tokens.border),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Takes up remaining space
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(tokens.mapBackground),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.phase == ScannerPhase.LIVE && hasCameraPermission) {
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
                                    } catch (e: Exception) {
                                        // Handle error
                                    }
                                }, ContextCompat.getMainExecutor(ctx))
                                previewView
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                        
                        // Capture Button Overlay
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
                                            
                                            viewModel.onPhotoReady(
                                                source = ImageSource { ByteArrayInputStream(jpegBytes) },
                                                previewModel = rotatedBitmap
                                            )
                                            image.close()
                                        }
                                        
                                        override fun onError(exception: ImageCaptureException) {
                                            viewModel.onCaptureFailed()
                                        }
                                    }
                                )
                            },
                            containerColor = tokens.aiMark,
                            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Capture", tint = tokens.aiInk)
                        }
                    } else if (uiState.phase == ScannerPhase.LIVE && !hasCameraPermission) {
                        Text("Camera permission required", color = tokens.muted)
                    } else {
                        // Display captured image or status
                        if (uiState.phase == ScannerPhase.ANALYZING) {
                            CircularProgressIndicator(color = tokens.aiMark)
                        } else if (uiState.status is ScanStatus.ModelUnavailable) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.CloudOff, contentDescription = null, tint = tokens.muted, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Offline model not loaded yet.", color = tokens.text)
                                Text((uiState.status as ScanStatus.ModelUnavailable).reason, color = tokens.muted, style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = viewModel::onRetake,
                                    colors = ButtonDefaults.buttonColors(containerColor = tokens.startFill, contentColor = tokens.onStart)
                                ) {
                                    Text("Retake Photo")
                                }
                            }
                        }
                    }
                }
            }

            if (uiState.phase == ScannerPhase.REVIEW && uiState.items.isNotEmpty()) {
                val totalCalories = uiState.items.sumOf { it.food.kcalPer100g * it.grams / 100.0 }.toInt()
                val totalProtein = uiState.items.sumOf { it.food.proteinPer100g * it.grams / 100.0 }.toInt()
                val totalCarbs = uiState.items.sumOf { it.food.carbsPer100g * it.grams / 100.0 }.toInt()
                val totalFat = uiState.items.sumOf { it.food.fatPer100g * it.grams / 100.0 }.toInt()

                // Thermal MealDeck Macros
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Thermal MealDeck", style = MaterialTheme.typography.titleMedium, color = tokens.text, fontWeight = FontWeight.Bold)
                            Text("$totalCalories kcal", style = MaterialTheme.typography.titleMedium, color = tokens.energyMark, fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MacroNutrientCard("Protein", "${totalProtein}g", tokens.proteinMark, tokens)
                            MacroNutrientCard("Carbs", "${totalCarbs}g", tokens.carbsMark, tokens)
                            MacroNutrientCard("Fat", "${totalFat}g", tokens.fatMark, tokens)
                        }

                        Button(
                            onClick = viewModel::onSave,
                            colors = ButtonDefaults.buttonColors(containerColor = tokens.aiMark, contentColor = tokens.aiInk),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Log Meal to Journal")
                        }
                        
                        TextButton(onClick = viewModel::onRetake, modifier = Modifier.fillMaxWidth()) {
                            Text("Retake Photo", color = tokens.muted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MacroNutrientCard(label: String, value: String, accent: Color, tokens: com.calovelo.app.presentation.theme.CaloTokens) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = tokens.background,
        border = BorderStroke(1.dp, tokens.border),
        modifier = Modifier.width(100.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(accent)
            )
            Spacer(Modifier.height(6.dp))
            Text(label, style = MaterialTheme.typography.bodySmall, color = tokens.muted)
            Text(value, style = MaterialTheme.typography.titleSmall, color = tokens.text, fontWeight = FontWeight.Bold)
        }
    }
}
