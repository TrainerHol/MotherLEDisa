package com.motherledisa.ui.control.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.motherledisa.domain.model.TowerState

/**
 * Visual preview of the LED tower showing current color state.
 *
 * Per UI-SPEC:
 * - Segment width: 60% of available width
 * - Segment gap: 4dp between segments
 * - Corner radius: 8dp
 * - Tower base: Dark gray circle below bottom segment
 */
@Composable
fun TowerPreviewCanvas(
    towerState: TowerState,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .semantics { contentDescription = "Tower preview showing current color" }
    ) {
        val segmentCount = towerState.segmentCount
        val segmentHeight = size.height / segmentCount
        val segmentWidth = size.width * 0.6f  // 60% width per UI-SPEC
        val offsetX = (size.width - segmentWidth) / 2
        val gap = 4.dp.toPx()
        val cornerRadius = 8.dp.toPx()

        // Draw tower segments from bottom to top
        for (i in 0 until segmentCount) {
            val segmentColor = towerState.segmentColors[i]
                ?: towerState.currentColor
            val y = size.height - (i + 1) * segmentHeight

            // Segment body
            drawRoundRect(
                color = if (towerState.isPoweredOn) segmentColor else Color.DarkGray,
                topLeft = Offset(offsetX, y + gap / 2),
                size = Size(segmentWidth, segmentHeight - gap),
                cornerRadius = CornerRadius(cornerRadius, cornerRadius)
            )

            // Subtle border for depth
            drawRoundRect(
                color = Color.White.copy(alpha = 0.2f),
                topLeft = Offset(offsetX, y + gap / 2),
                size = Size(segmentWidth, segmentHeight - gap),
                cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // Tower base
        drawCircle(
            color = Color(0xFF333333),
            radius = segmentWidth / 2 + 8.dp.toPx(),
            center = Offset(size.width / 2, size.height + 10.dp.toPx())
        )
    }
}
