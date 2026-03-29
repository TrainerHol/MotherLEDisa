package com.motherledisa.ui.sound.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Sensitivity slider for microphone threshold (0-100).
 * Per D-07: Single slider for sensitivity control.
 *
 * @param sensitivity Current sensitivity value 0-100
 * @param onSensitivityChanged Called when slider value changes
 * @param enabled Whether slider is enabled
 */
@Composable
fun SensitivitySlider(
    sensitivity: Int,
    onSensitivityChanged: (Int) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Sensitivity",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "$sensitivity%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Slider(
            value = sensitivity.toFloat(),
            onValueChange = { onSensitivityChanged(it.toInt()) },
            valueRange = 0f..100f,
            steps = 9,  // 10 positions: 0, 10, 20... 100
            enabled = enabled,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Low",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "High",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
