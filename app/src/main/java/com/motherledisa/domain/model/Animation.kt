package com.motherledisa.domain.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

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

/**
 * Extract a color palette from animation keyframes.
 * Per D-05: User can pick from a saved animation preset to extract its color palette.
 *
 * @param maxColors Maximum colors to extract (default 5 per D-05)
 * @return SoundPalette with unique colors from keyframes
 */
fun Animation.extractPalette(maxColors: Int = 5): SoundPalette {
    val uniqueColors = keyframes
        .map { Color(it.color) }
        .distinctBy { color ->
            // Group similar colors by hue bucket (within ~15 degrees)
            val hsl = floatArrayOf(0f, 0f, 0f)
            android.graphics.Color.colorToHSL(color.toArgb(), hsl)
            (hsl[0] / 15).toInt()  // Bucket by hue
        }
        .take(maxColors)

    return if (uniqueColors.isEmpty()) {
        SoundPalette.DEFAULT
    } else {
        SoundPalette(colors = uniqueColors)
    }
}
