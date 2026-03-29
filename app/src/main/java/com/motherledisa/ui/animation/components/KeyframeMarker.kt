package com.motherledisa.ui.animation.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.motherledisa.domain.model.Keyframe

/**
 * Keyframe marker drawing functions.
 * Per D-02: Diamond markers on tracks, colored by keyframe's color value.
 */
object KeyframeMarker {
    /** Size of keyframe diamond (half-width/height) */
    val SIZE = 12.dp

    /** Hit test radius for tap/drag detection */
    val HIT_RADIUS = 16.dp

    /**
     * Draw a diamond-shaped keyframe marker.
     * @param center Center position of the diamond
     * @param size Half-size (distance from center to point)
     * @param color Fill color (from keyframe's color value per D-02)
     * @param isSelected Whether to show selection highlight
     */
    fun DrawScope.drawKeyframeDiamond(
        center: Offset,
        size: Float,
        color: Color,
        isSelected: Boolean = false
    ) {
        val path = Path().apply {
            moveTo(center.x, center.y - size)  // Top point
            lineTo(center.x + size, center.y)  // Right point
            lineTo(center.x, center.y + size)  // Bottom point
            lineTo(center.x - size, center.y)  // Left point
            close()
        }

        // Fill with keyframe color
        drawPath(path, color)

        // Border
        drawPath(
            path,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
            style = Stroke(width = if (isSelected) 3.dp.toPx() else 1.dp.toPx())
        )

        // Selection glow effect
        if (isSelected) {
            drawPath(
                path,
                color = Color.White.copy(alpha = 0.3f),
                style = Stroke(width = 6.dp.toPx())
            )
        }
    }

    /**
     * Hit test to check if a point is within a keyframe marker.
     * @param keyframe The keyframe to test
     * @param point The point to test (in timeline coordinates)
     * @param pixelsPerMs Current zoom level
     * @param trackY Y position of the keyframe's track
     * @param trackHeight Height of the track
     * @param labelWidth Width of segment label area
     * @return true if point is within hit radius of keyframe
     */
    fun hitTest(
        keyframe: Keyframe,
        point: Offset,
        pixelsPerMs: Float,
        trackY: Float,
        trackHeight: Float,
        labelWidth: Float = 40f
    ): Boolean {
        val kfX = keyframe.timeMs * pixelsPerMs + labelWidth
        val kfY = trackY + trackHeight / 2
        val hitRadius = HIT_RADIUS.value

        val dx = point.x - kfX
        val dy = point.y - kfY

        return (dx * dx + dy * dy) <= hitRadius * hitRadius
    }
}
