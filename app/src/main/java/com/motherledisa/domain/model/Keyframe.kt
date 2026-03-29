package com.motherledisa.domain.model

import kotlinx.serialization.Serializable

/**
 * Single keyframe in an animation timeline.
 * Per D-06: Each keyframe controls Color, Segment, Brightness, optional Effect.
 */
@Serializable
data class Keyframe(
    /** Time position in milliseconds */
    val timeMs: Long,
    /** Tower segment index (0-4 for 5 segments per D-05) */
    val segment: Int,
    /** Color as ARGB int for serialization */
    val color: Int,
    /** Brightness 0-100 */
    val brightness: Int = 100,
    /** Optional hardware effect trigger (null = color only) */
    val effectId: Byte? = null,
    /** Interpolation mode per D-07 */
    val interpolation: InterpolationMode = InterpolationMode.SMOOTH
)
