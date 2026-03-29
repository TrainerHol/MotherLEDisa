package com.motherledisa.ui.animation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.motherledisa.domain.model.Animation
import com.motherledisa.domain.model.Keyframe
import com.motherledisa.ui.animation.components.PlayheadLine.drawPlayhead
import com.motherledisa.ui.animation.components.TimeRuler.drawTimeRuler
import com.motherledisa.ui.animation.components.TimelineTrack.drawTrack

/**
 * Main timeline canvas for animation editing.
 * Per D-01: Horizontal timeline, time flows left-to-right, segment tracks stacked vertically.
 * Per D-03: Long-press to add keyframe.
 * Per D-04: Draggable playhead.
 */
@Composable
fun TimelineCanvas(
    animation: Animation,
    playheadTimeMs: Long,
    selectedKeyframe: Keyframe?,
    onPlayheadDragged: (Long) -> Unit,
    onKeyframeDragged: (Keyframe, Long) -> Unit,
    onKeyframeTapped: (Keyframe) -> Unit,
    onTrackLongPress: (segment: Int, timeMs: Long) -> Unit,
    onEmptyTap: () -> Unit,
    modifier: Modifier = Modifier,
    pixelsPerMs: Float = 0.1f  // Default zoom: 100px per second
) {
    val scrollState = rememberScrollState()
    val density = LocalDensity.current

    // Calculate total width based on duration and zoom
    val totalWidth = with(density) {
        (animation.durationMs * pixelsPerMs).toDp() + 80.dp  // Extra for labels + padding
    }

    // Drag state
    var isDraggingPlayhead by remember { mutableStateOf(false) }
    var draggingKeyframe by remember { mutableStateOf<Keyframe?>(null) }

    Box(
        modifier = modifier
            .background(Color(0xFF1E1E1E))
            .horizontalScroll(scrollState)
    ) {
        Canvas(
            modifier = Modifier
                .width(totalWidth)
                .fillMaxHeight()
                .pointerInput(animation, pixelsPerMs) {
                    detectTapGestures(
                        onTap = { offset ->
                            handleTap(
                                offset = offset,
                                animation = animation,
                                pixelsPerMs = pixelsPerMs,
                                onKeyframeTapped = onKeyframeTapped,
                                onEmptyTap = onEmptyTap,
                                density = density
                            )
                        },
                        onLongPress = { offset ->
                            handleLongPress(
                                offset = offset,
                                pixelsPerMs = pixelsPerMs,
                                onTrackLongPress = onTrackLongPress,
                                density = density
                            )
                        }
                    )
                }
                .pointerInput(animation, playheadTimeMs, pixelsPerMs) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val result = handleDragStart(
                                offset = offset,
                                animation = animation,
                                playheadTimeMs = playheadTimeMs,
                                pixelsPerMs = pixelsPerMs,
                                density = density
                            )
                            isDraggingPlayhead = result.first
                            draggingKeyframe = result.second
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val newTimeMs = ((change.position.x - 40.dp.toPx()) / pixelsPerMs)
                                .toLong()
                                .coerceIn(0, animation.durationMs)

                            when {
                                isDraggingPlayhead -> onPlayheadDragged(newTimeMs)
                                draggingKeyframe != null -> {
                                    onKeyframeDragged(draggingKeyframe!!, newTimeMs)
                                }
                            }
                        },
                        onDragEnd = {
                            isDraggingPlayhead = false
                            draggingKeyframe = null
                        }
                    )
                }
        ) {
            val rulerHeight = TimeRuler.HEIGHT.toPx()
            val trackHeight = TimelineTrack.TRACK_HEIGHT.toPx()
            val trackGap = TimelineTrack.TRACK_GAP.toPx()
            val labelWidth = 40.dp.toPx()

            // Draw time ruler at top
            drawTimeRuler(
                durationMs = animation.durationMs,
                pixelsPerMs = pixelsPerMs
            )

            // Draw segment tracks (5 segments per D-05)
            for (segment in 0 until 5) {
                val trackY = rulerHeight + segment * (trackHeight + trackGap)
                val segmentKeyframes = animation.keyframesForSegment(segment)

                drawTrack(
                    segment = segment,
                    keyframes = segmentKeyframes,
                    trackY = trackY,
                    pixelsPerMs = pixelsPerMs,
                    selectedKeyframe = selectedKeyframe
                )
            }

            // Draw playhead on top
            drawPlayhead(
                timeMs = playheadTimeMs,
                pixelsPerMs = pixelsPerMs,
                labelWidth = labelWidth
            )
        }
    }
}

/**
 * Handle tap gesture - select keyframe or deselect.
 */
private fun handleTap(
    offset: Offset,
    animation: Animation,
    pixelsPerMs: Float,
    onKeyframeTapped: (Keyframe) -> Unit,
    onEmptyTap: () -> Unit,
    density: Density
) {
    val rulerHeight = with(density) { TimeRuler.HEIGHT.toPx() }
    val trackHeight = with(density) { TimelineTrack.TRACK_HEIGHT.toPx() }
    val trackGap = with(density) { TimelineTrack.TRACK_GAP.toPx() }
    val labelWidth = with(density) { 40.dp.toPx() }

    // Check each keyframe for hit
    for (keyframe in animation.keyframes) {
        val segment = keyframe.segment
        val trackY = rulerHeight + segment * (trackHeight + trackGap)

        if (KeyframeMarker.hitTest(
                keyframe = keyframe,
                point = offset,
                pixelsPerMs = pixelsPerMs,
                trackY = trackY,
                trackHeight = trackHeight,
                labelWidth = labelWidth
            )) {
            onKeyframeTapped(keyframe)
            return
        }
    }

    // No keyframe hit - deselect
    onEmptyTap()
}

/**
 * Handle long press - add keyframe at position.
 * Per D-03: Long-press on track to add new keyframe.
 */
private fun handleLongPress(
    offset: Offset,
    pixelsPerMs: Float,
    onTrackLongPress: (segment: Int, timeMs: Long) -> Unit,
    density: Density
) {
    val rulerHeight = with(density) { TimeRuler.HEIGHT.toPx() }
    val labelWidth = with(density) { 40.dp.toPx() }

    val segment = TimelineTrack.getSegmentFromY(offset.y, rulerHeight)
    if (segment >= 0) {
        val timeMs = ((offset.x - labelWidth) / pixelsPerMs).toLong().coerceAtLeast(0)
        onTrackLongPress(segment, timeMs)
    }
}

/**
 * Handle drag start - determine if dragging playhead or keyframe.
 * @return Pair of (isDraggingPlayhead, draggingKeyframe)
 */
private fun handleDragStart(
    offset: Offset,
    animation: Animation,
    playheadTimeMs: Long,
    pixelsPerMs: Float,
    density: Density
): Pair<Boolean, Keyframe?> {
    val labelWidth = with(density) { 40.dp.toPx() }
    val playheadX = playheadTimeMs * pixelsPerMs + labelWidth

    // Check playhead first
    if (PlayheadLine.hitTestHandle(offset, playheadX)) {
        return Pair(true, null)
    }

    // Check keyframes
    val rulerHeight = with(density) { TimeRuler.HEIGHT.toPx() }
    val trackHeight = with(density) { TimelineTrack.TRACK_HEIGHT.toPx() }
    val trackGap = with(density) { TimelineTrack.TRACK_GAP.toPx() }

    for (keyframe in animation.keyframes) {
        val segment = keyframe.segment
        val trackY = rulerHeight + segment * (trackHeight + trackGap)

        if (KeyframeMarker.hitTest(
                keyframe = keyframe,
                point = offset,
                pixelsPerMs = pixelsPerMs,
                trackY = trackY,
                trackHeight = trackHeight,
                labelWidth = labelWidth
            )) {
            return Pair(false, keyframe)
        }
    }

    return Pair(false, null)
}
