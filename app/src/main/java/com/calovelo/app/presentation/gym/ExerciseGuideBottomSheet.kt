package com.calovelo.app.presentation.gym

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.calovelo.app.domain.model.Exercise
import com.calovelo.app.presentation.theme.CaloTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseGuideBottomSheet(
    exercise: Exercise,
    onDismiss: () -> Unit
) {
    val tokens = CaloTheme.tokens

    // We will dynamically load animations like "bench_press.json" based on exercise.id
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Asset("animations/gym_guide_placeholder.json")
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = tokens.background,
        dragHandle = { BottomSheetDefaults.DragHandle(color = tokens.muted) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        exercise.muscleGroup.name.replace("_", " "),
                        style = MaterialTheme.typography.labelSmall,
                        color = tokens.energyMark,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        exercise.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = tokens.text,
                        fontWeight = FontWeight.Black
                    )
                }
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(tokens.surface.copy(alpha=0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = tokens.text)
                }
            }

            // 3D Lottie Animation Box
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = tokens.surface.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth().height(200.dp)
            ) {
                if (composition != null) {
                    LottieAnimation(
                        composition = composition,
                        iterations = LottieConstants.IterateForever,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Animation Loading...", color = tokens.muted)
                    }
                }
            }

            // Instructions
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = tokens.energyMark, modifier = Modifier.size(20.dp))
                    Text("Execution Guide", color = tokens.text, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
                Text(
                    text = exercise.instructions,
                    color = tokens.muted,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
