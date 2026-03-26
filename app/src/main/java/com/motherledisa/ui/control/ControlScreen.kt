package com.motherledisa.ui.control

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Tower control screen for color, brightness, and effects.
 * Placeholder - full implementation in Plan 03.
 *
 * @param deviceAddress Specific device to control, or null for "All devices" mode
 */
@Composable
fun ControlScreen(
    deviceAddress: String? = null
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (deviceAddress != null) {
                "Control: $deviceAddress\n(Coming in Plan 03)"
            } else {
                "Control: All devices\n(Coming in Plan 03)"
            },
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
