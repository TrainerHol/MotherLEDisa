package com.motherledisa.domain.model

import androidx.compose.ui.graphics.Color

/**
 * UI state for a connected tower's current settings.
 * Updated in real-time as controls change.
 */
data class TowerState(
    /** Power state (on/off) */
    val isPoweredOn: Boolean = false,
    /** Current RGB color displayed */
    val currentColor: Color = Color.White,
    /** Brightness level (0-100) */
    val brightness: Int = 100,
    /** Currently active hardware effect, if any */
    val activeEffect: Effect? = null,
    /** Effect playback speed (0-100) */
    val effectSpeed: Int = 50,
    /** Number of LED segments on tower (for preview visualization) */
    val segmentCount: Int = 5,
    /** Per-segment color map for preview (segment index -> color) */
    val segmentColors: Map<Int, Color> = emptyMap()
)
