package com.motherledisa.domain.model

/**
 * Animation containing keyframes and playback settings.
 * Domain model used by UI and playback engine.
 */
data class Animation(
    val id: Long = 0,
    val name: String,
    val durationMs: Long,
    val keyframes: List<Keyframe>,
    val loopMode: LoopMode = LoopMode.ONCE,
    val loopCount: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        /** Default 5-second animation with no keyframes */
        fun empty() = Animation(
            name = "Untitled",
            durationMs = 5000L,
            keyframes = emptyList()
        )
    }

    /** Get keyframes for a specific segment, sorted by time */
    fun keyframesForSegment(segment: Int): List<Keyframe> =
        keyframes.filter { it.segment == segment }.sortedBy { it.timeMs }
}
