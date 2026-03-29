package com.motherledisa.ui.animation.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp

/**
 * Playhead drawing functions.
 * Per D-04: Draggable vertical line spanning all tracks.
 */
object PlayheadLine {
    /** Width of the playhead line */
    private val LINE_WIDTH = 2.dp

    /** Size of the playhead handle at top */
    private val HANDLE_SIZE = 10.dp

    /** Playhead color (bright for visibility) */
    private val PLAYHEAD_COLOR = Color(0xFFFF5252)  // Red accent

    /**
     * Draw the playhead at a specific time position.
     * @param timeMs Current playhead time in milliseconds
     * @param pixelsPerMs Zoom level
     * @param scrollOffset Horizontal scroll
     * @param labelWidth Width of segment label area
     */
    fun DrawScope.drawPlayhead(
        timeMs: Long,
        pixelsPerMs: Float,
        scrollOffset: Float = 0f,
        labelWidth: Float = 40f
    ) {
        val x = timeMs * pixelsPerMs - scrollOffset + labelWidth

        // Don't draw if outside visible area
        if (x < labelWidth || x > size.width) return

        // Vertical line spanning full height
        drawLine(
            color = PLAYHEAD_COLOR,
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = LINE_WIDTH.toPx()
        )

        // Handle triangle at top
        val handleSize = HANDLE_SIZE.toPx()
        val handlePath = Path().apply {
            moveTo(x - handleSize, 0f)
            lineTo(x + handleSize, 0f)
            lineTo(x, handleSize * 1.5f)
            close()
        }
        drawPath(handlePath, PLAYHEAD_COLOR)
    }

    /**
     * Hit test for playhead handle.
     * @param point Touch point
     * @param playheadX X position of playhead
     * @return true if touch is on playhead handle
     */
    fun hitTestHandle(point: Offset, playheadX: Float): Boolean {
        val handleRadius = HANDLE_SIZE.value * 1.5f
        return point.y < handleRadius * 2 &&
               kotlin.math.abs(point.x - playheadX) < handleRadius
    }
}
