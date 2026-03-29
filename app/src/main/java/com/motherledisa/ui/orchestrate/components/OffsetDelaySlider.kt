package com.motherledisa.ui.orchestrate.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Slider for configuring offset delay between tower starts.
 * Per D-06: Single delay slider (0-2000ms range) applied between consecutive towers.
 *
 * Example: With 500ms delay and 3 towers:
 * - Tower 1 starts at 0ms
 * - Tower 2 starts at 500ms
 * - Tower 3 starts at 1000ms
 */
@Composable
fun OffsetDelaySlider(
    delayMs: Int,
    onDelayChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Offset Delay",
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${delayMs}ms",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = delayMs.toFloat(),
            onValueChange = { onDelayChanged(it.toInt()) },
            valueRange = 0f..2000f,
            steps = 19, // 100ms increments: 0, 100, 200, ..., 2000
            enabled = enabled,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Delay between each tower starting",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
