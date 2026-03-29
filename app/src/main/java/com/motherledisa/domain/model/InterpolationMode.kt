package com.motherledisa.domain.model

import kotlinx.serialization.Serializable

/**
 * Interpolation mode for transitions between keyframes.
 * Per D-07, D-08: Determines how values change between keyframe points.
 */
@Serializable
enum class InterpolationMode {
    /** Smooth transition using HSV interpolation (D-07, D-08) */
    SMOOTH,
    /** Hold value until next keyframe (step/hold mode) (D-07) */
    STEP
}
