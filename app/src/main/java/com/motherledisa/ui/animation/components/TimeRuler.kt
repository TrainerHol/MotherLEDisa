package com.motherledisa.ui.animation.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp

/**
 * Time ruler drawing functions for timeline header.
 * Per D-01: Horizontal timeline with time flowing left-to-right.
 */
object TimeRuler {
    /** Height of the time ruler area */
    val HEIGHT = 40.dp

    /** Major tick interval in milliseconds (1 second) */
    private const val MAJOR_TICK_MS = 1000L

    /** Minor tick interval in milliseconds (250ms) */
    private const val MINOR_TICK_MS = 250L

    /**
     * Draw time ruler with tick marks and labels.
     * @param durationMs Total animation duration
     * @param pixelsPerMs Zoom level (pixels per millisecond)
     * @param scrollOffset Current horizontal scroll position
     */
    fun DrawScope.drawTimeRuler(
        durationMs: Long,
        pixelsPerMs: Float,
        scrollOffset: Float = 0f,
        textColor: Color = Color.White
    ) {
        val rulerHeight = HEIGHT.toPx()
        val majorTickHeight = 16.dp.toPx()
        val minorTickHeight = 8.dp.toPx()

        // Background
        drawRect(
            color = Color(0xFF1A1A1A),
            size = size.copy(height = rulerHeight)
        )

        // Calculate visible time range
        val visibleStartMs = (scrollOffset / pixelsPerMs).toLong().coerceAtLeast(0)
        val visibleEndMs = ((scrollOffset + size.width) / pixelsPerMs).toLong().coerceAtMost(durationMs)

        // Draw minor ticks
        var tickMs = (visibleStartMs / MINOR_TICK_MS) * MINOR_TICK_MS
        while (tickMs <= visibleEndMs) {
            val x = tickMs * pixelsPerMs - scrollOffset
            val isMajor = tickMs % MAJOR_TICK_MS == 0L

            drawLine(
                color = if (isMajor) Color.White else Color.Gray,
                start = Offset(x, rulerHeight - if (isMajor) majorTickHeight else minorTickHeight),
                end = Offset(x, rulerHeight),
                strokeWidth = if (isMajor) 2f else 1f
            )

            tickMs += MINOR_TICK_MS
        }

        // Bottom border
        drawLine(
            color = Color.Gray,
            start = Offset(0f, rulerHeight),
            end = Offset(size.width, rulerHeight),
            strokeWidth = 1f
        )
    }

    /**
     * Format milliseconds as time string (e.g., "1.5s").
     */
    fun formatTime(ms: Long): String {
        val seconds = ms / 1000f
        return if (seconds == seconds.toLong().toFloat()) {
            "${seconds.toLong()}s"
        } else {
            String.format("%.1fs", seconds)
        }
    }
}
