package com.motherledisa.ui.control.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.motherledisa.ui.theme.OnSurfaceSecondary
import com.motherledisa.ui.theme.PrimaryAccent

/**
 * Brightness control slider.
 *
 * Per D-10:
 * - 0-100% range
 * - Continuous updates while dragging
 * - ViewModel debounces BLE commands to ~30fps
 */
@Composable
fun BrightnessSlider(
    brightness: Int,
    onBrightnessChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Brightness",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "$brightness%",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceSecondary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Slider(
            value = brightness.toFloat(),
            onValueChange = { onBrightnessChanged(it.toInt()) },
            valueRange = 0f..100f,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = PrimaryAccent,
                activeTrackColor = PrimaryAccent
            )
        )
    }
}
