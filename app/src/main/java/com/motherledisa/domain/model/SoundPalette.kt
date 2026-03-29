package com.motherledisa.domain.model

import androidx.compose.ui.graphics.Color

/**
 * Color palette for sound-reactive mode.
 * Per D-05: 2-5 colors for sound mode.
 * Per Research pitfall #4: Tower uses single base color, not color sequences.
 *
 * @property colors List of 2-5 colors in the palette
 * @property primaryIndex Index of the primary color used as base for sound effects
 */
data class SoundPalette(
    val colors: List<Color>,
    val primaryIndex: Int = 0
) {
    init {
        require(colors.isNotEmpty()) { "Palette must have at least 1 color" }
        require(colors.size <= 5) { "Palette can have at most 5 colors" }
        require(primaryIndex in colors.indices) { "Primary index must be valid" }
    }

    /** The primary color used as base for sound-reactive effects */
    val primaryColor: Color get() = colors[primaryIndex]

    companion object {
        /** Default palette with common colors */
        val DEFAULT = SoundPalette(
            colors = listOf(
                Color.Red,
                Color(0xFFFF8800),  // Orange
                Color.Yellow,
                Color.Green,
                Color.Cyan
            ),
            primaryIndex = 0
        )

        /** Create palette from single color */
        fun single(color: Color) = SoundPalette(listOf(color))
    }
}
