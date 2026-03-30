package com.motherledisa.domain.animation

import com.motherledisa.domain.model.Animation
import com.motherledisa.domain.model.InterpolationMode
import com.motherledisa.domain.model.Keyframe
/**
 * Result of evaluating animation at a specific time.
 * Contains color and brightness for each segment.
 */
data class FrameState(
    /** Map of segment index to ARGB color */
    val segmentColors: Map<Int, Int>,
    /** Map of segment index to brightness (0-100) */
    val segmentBrightness: Map<Int, Int>
)

/**
 * Evaluates animation keyframes at any point in time.
 * Handles interpolation between keyframes per D-07.
 */
class AnimationEvaluator {

    /** Number of tower segments per D-05 */
    private val segmentCount = 5

    /**
     * Evaluate animation at a specific time.
     * @param animation The animation to evaluate
     * @param timeMs Current time in milliseconds
     * @return FrameState with colors and brightness for each segment
     */
    fun evaluateAt(animation: Animation, timeMs: Long): FrameState {
        val colors = mutableMapOf<Int, Int>()
        val brightness = mutableMapOf<Int, Int>()

        for (segment in 0 until segmentCount) {
            val segmentKeyframes = animation.keyframesForSegment(segment)

            if (segmentKeyframes.isEmpty()) {
                // No keyframes for this segment - use default
                colors[segment] = 0xFFFFFFFF.toInt()  // White
                brightness[segment] = 100
                continue
            }

            // Find surrounding keyframes
            val (before, after) = findSurroundingKeyframes(segmentKeyframes, timeMs)

            when {
                before == null && after != null -> {
                    // Before first keyframe - use first keyframe values
                    colors[segment] = after.color
                    brightness[segment] = after.brightness
                }
                before != null && after == null -> {
                    // After last keyframe - use last keyframe values
                    colors[segment] = before.color
                    brightness[segment] = before.brightness
                }
                before != null && after != null -> {
                    // Between keyframes - interpolate based on mode
                    val fraction = calculateFraction(before.timeMs, after.timeMs, timeMs)

                    when (before.interpolation) {
                        InterpolationMode.SMOOTH -> {
                            colors[segment] = ColorInterpolator.interpolateHSV(
                                before.color, after.color, fraction
                            )
                            brightness[segment] = ColorInterpolator.interpolateBrightness(
                                before.brightness, after.brightness, fraction
                            )
                        }
                        InterpolationMode.STEP -> {
                            // Hold previous value until next keyframe
                            colors[segment] = before.color
                            brightness[segment] = before.brightness
                        }
                    }
                }
                else -> {
                    // Should not happen
                    colors[segment] = 0xFFFFFFFF.toInt()
                    brightness[segment] = 100
                }
            }
        }

        return FrameState(colors, brightness)
    }

    /**
     * Find keyframes immediately before and after given time.
     */
    private fun findSurroundingKeyframes(
        keyframes: List<Keyframe>,
        timeMs: Long
    ): Pair<Keyframe?, Keyframe?> {
        val sorted = keyframes.sortedBy { it.timeMs }

        var before: Keyframe? = null
        var after: Keyframe? = null

        for (kf in sorted) {
            if (kf.timeMs <= timeMs) {
                before = kf
            } else {
                after = kf
                break
            }
        }

        return Pair(before, after)
    }

    /**
     * Calculate interpolation fraction between two time points.
     */
    private fun calculateFraction(startMs: Long, endMs: Long, currentMs: Long): Float {
        if (endMs == startMs) return 0f
        return ((currentMs - startMs).toFloat() / (endMs - startMs)).coerceIn(0f, 1f)
    }
}
