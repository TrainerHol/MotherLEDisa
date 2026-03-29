package com.motherledisa.domain.model

import kotlinx.serialization.Serializable

/**
 * Loop behavior for animation playback.
 * Per D-11: Supports various loop patterns for animations.
 */
@Serializable
enum class LoopMode {
    /** Play once and stop */
    ONCE,
    /** Play N times (D-11: 2x, 3x, etc.) */
    COUNT,
    /** Loop forever (D-11) */
    INFINITE,
    /** Play forward then reverse: A->B->A (D-11) */
    PING_PONG
}
