package com.motherledisa.ui.orchestrate.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.motherledisa.domain.orchestration.OrchestrationMode

/**
 * Horizontal segmented control for orchestration mode selection.
 * Per D-03: Quick visual toggle between Mirror | Offset | Cascade | Independent.
 * Per D-04: Mode selection is global across all towers.
 */
@Composable
fun OrchestrationModeSelector(
    selectedMode: OrchestrationMode,
    onModeSelected: (OrchestrationMode) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val modes = OrchestrationMode.entries

    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        modes.forEachIndexed { index, mode ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size),
                onClick = { onModeSelected(mode) },
                selected = mode == selectedMode,
                enabled = enabled,
                label = { Text(mode.displayName) }
            )
        }
    }
}
