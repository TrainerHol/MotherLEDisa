package com.motherledisa.domain.model

/**
 * Current playback state for animation preview/execution.
 */
enum class PlaybackState {
    /** Animation is not playing */
    STOPPED,
    /** Animation is actively playing */
    PLAYING,
    /** Animation is paused at current position */
    PAUSED
}
