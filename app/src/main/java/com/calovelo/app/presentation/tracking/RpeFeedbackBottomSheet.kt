package com.calovelo.app.presentation.tracking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.calovelo.app.core.format.WorkoutFormatters
import com.calovelo.app.domain.model.FeelingTag
import com.calovelo.app.domain.model.PendingFeedback
import com.calovelo.app.domain.model.RpeScale
import com.calovelo.app.domain.repository.WorkoutRepository
import kotlin.math.roundToInt

/**
 * Modal sheet shown right after a workout is finished and saved. The workout (including its
 * route polyline) is already in Room at this point; "Save Session" attaches the RPE score,
 * feeling tag and notes to it. Swiping the sheet away skips feedback.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RpeFeedbackBottomSheet(
    pending: PendingFeedback,
    isSaving: Boolean,
    onSave: (rpeScore: Int, feelingTag: FeelingTag?, notes: String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var rpeValue by rememberSaveable { mutableStateOf(RpeScale.DEFAULT.toFloat()) }
    var selectedTagName by rememberSaveable { mutableStateOf<String?>(null) }
    var notes by rememberSaveable { mutableStateOf("") }

    val rpeScore = rpeValue.roundToInt().coerceIn(RpeScale.MIN, RpeScale.MAX)
    val rpeLabel = RpeScale.label(rpeScore)

    ModalBottomSheet(
        onDismissRequest = { if (!isSaving) onDismiss() },
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding()
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "How did that feel?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${pending.activityType.displayName} · " +
                        "${WorkoutFormatters.formatDistanceKm(pending.distanceMeters)} km · " +
                        WorkoutFormatters.formatElapsed(pending.durationSeconds),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Perceived exertion", style = MaterialTheme.typography.titleSmall)
                Text(
                    rpeLabel,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Slider(
                    value = rpeValue,
                    onValueChange = { rpeValue = it },
                    valueRange = RpeScale.MIN.toFloat()..RpeScale.MAX.toFloat(),
                    steps = RpeScale.MAX - RpeScale.MIN - 1, // 8 stops between the endpoints
                    enabled = !isSaving,
                    modifier = Modifier.semantics { stateDescription = rpeLabel }
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("How do you feel?", style = MaterialTheme.typography.titleSmall)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FeelingTag.entries.forEach { tag ->
                        FilterChip(
                            selected = selectedTagName == tag.name,
                            onClick = {
                                selectedTagName = if (selectedTagName == tag.name) null else tag.name
                            },
                            enabled = !isSaving,
                            label = { Text(tag.label) }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { if (it.length <= WorkoutRepository.MAX_NOTES_LENGTH) notes = it },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving,
                label = { Text("Notes") },
                placeholder = { Text("Route, weather, how your legs felt…") },
                minLines = 3,
                maxLines = 6,
                supportingText = { Text("${notes.length}/${WorkoutRepository.MAX_NOTES_LENGTH}") }
            )

            Button(
                onClick = { onSave(rpeScore, FeelingTag.fromName(selectedTagName), notes) },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.size(8.dp))
                }
                Text("Save Session")
            }
        }
    }
}
