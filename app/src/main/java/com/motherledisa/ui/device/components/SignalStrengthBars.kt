package com.motherledisa.ui.device.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Signal strength bars visualization based on RSSI.
 * Thresholds from UI-SPEC.md (lines 260-275).
 *
 * @param rssi Signal strength in dBm, null for unknown
 */
@Composable
fun SignalStrengthBars(
    rssi: Int?,
    modifier: Modifier = Modifier
) {
    // RSSI to bars mapping per UI-SPEC
    val filledBars = when {
        rssi == null -> 0
        rssi >= -50 -> 4  // Excellent
        rssi >= -65 -> 3  // Good
        rssi >= -80 -> 2  // Fair
        else -> 1         // Weak
    }

    val strengthLabel = when (filledBars) {
        4 -> "excellent"
        3 -> "good"
        2 -> "fair"
        1 -> "weak"
        else -> "unknown"
    }

    Canvas(
        modifier = modifier
            .size(width = 24.dp, height = 20.dp)
            .semantics { contentDescription = "Signal strength $strengthLabel" }
    ) {
        val barWidth = 4.dp.toPx()
        val gap = 2.dp.toPx()
        val barHeights = listOf(5.dp.toPx(), 9.dp.toPx(), 13.dp.toPx(), 17.dp.toPx())
        val cornerRadius = CornerRadius(1.dp.toPx())

        barHeights.forEachIndexed { index, barHeight ->
            val isFilled = index < filledBars
            val color = if (isFilled) {
                Color.White
            } else {
                Color.White.copy(alpha = 0.38f)
            }

            drawRoundRect(
                color = color,
                topLeft = Offset(
                    x = index * (barWidth + gap),
                    y = size.height - barHeight
                ),
                size = Size(barWidth, barHeight),
                cornerRadius = cornerRadius
            )
        }
    }
}
