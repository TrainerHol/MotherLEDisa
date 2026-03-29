package com.motherledisa.ui.animation.components

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.motherledisa.domain.model.Keyframe
import com.motherledisa.ui.animation.components.KeyframeMarker.drawKeyframeDiamond

/**
 * Track drawing functions for timeline segments.
 * Per D-05: One track per tower segment (5 segments = 5 horizontal tracks).
 */
object TimelineTrack {
    /** Height of each track */
    val TRACK_HEIGHT = 48.dp

    /** Vertical gap between tracks */
    val TRACK_GAP = 4.dp

    /** Track background colors alternating for visibility */
    private val TRACK_COLORS = listOf(
        Color(0xFF2A2A2A),
        Color(0xFF252525)
    )

    /**
     * Draw a single segment track with its keyframes.
     * @param segment Segment index (0-4)
     * @param keyframes Keyframes for this segment
     * @param trackY Y position of this track
     * @param pixelsPerMs Zoom level
     * @param scrollOffset Horizontal scroll
     * @param selectedKeyframe Currently selected keyframe (for highlight)
     */
    fun DrawScope.drawTrack(
        segment: Int,
        keyframes: List<Keyframe>,
        trackY: Float,
        pixelsPerMs: Float,
        scrollOffset: Float = 0f,
        selectedKeyframe: Keyframe? = null
    ) {
        val trackHeight = TRACK_HEIGHT.toPx()
        val cornerRadius = 4.dp.toPx()

        // Track background
        drawRoundRect(
            color = TRACK_COLORS[segment % TRACK_COLORS.size],
            topLeft = Offset(0f, trackY),
            size = Size(size.width, trackHeight),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius)
        )

        // Segment label area (left side)
        drawRoundRect(
            color = Color(0xFF333333),
            topLeft = Offset(0f, trackY),
            size = Size(40.dp.toPx(), trackHeight),
            cornerRadius = CornerRadius(cornerRadius, 0f)
        )

        // Draw keyframes on this track
        keyframes.forEach { keyframe ->
            val x = keyframe.timeMs * pixelsPerMs - scrollOffset + 40.dp.toPx()
            val centerY = trackY + trackHeight / 2

            if (x >= 40.dp.toPx() && x <= size.width) {
                drawKeyframeDiamond(
                    center = Offset(x, centerY),
                    size = 12.dp.toPx(),
                    color = Color(keyframe.color),
                    isSelected = keyframe == selectedKeyframe
                )
            }
        }
    }

    /**
     * Get segment index from Y coordinate.
     * @return Segment index (0-4) or -1 if outside tracks
     */
    fun getSegmentFromY(y: Float, rulerHeight: Float): Int {
        val trackHeight = TRACK_HEIGHT.value
        val trackGap = TRACK_GAP.value
        val totalTrackHeight = trackHeight + trackGap

        if (y < rulerHeight) return -1

        val trackIndex = ((y - rulerHeight) / totalTrackHeight).toInt()
        return if (trackIndex in 0..4) trackIndex else -1
    }
}
