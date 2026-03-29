package com.motherledisa.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes for MotherLEDisa.
 * Uses Kotlin Serialization for compile-time route safety (Navigation 2.8+).
 */
@Serializable
sealed class Screen {
    /**
     * Device discovery and list screen.
     * Shows nearby ELK-BLEDOM devices with connection status.
     */
    @Serializable
    data object DeviceList : Screen()

    /**
     * Tower control screen for color, brightness, and effects.
     * @param deviceAddress Optional - if null, shows "All devices" mode
     */
    @Serializable
    data class Control(val deviceAddress: String? = null) : Screen()

    /**
     * Sound-reactive configuration screen.
     * Per UX-04: Dedicated screen for sound-reactive configuration.
     * Configures tower's built-in microphone for autonomous sound response.
     */
    @Serializable
    data object SoundReactive : Screen()

    /**
     * Animation editor screen for creating keyframe animations.
     * Per UX-03: Dedicated screen for timeline animation editor.
     * @param animationId Optional - if null, creates new animation; if set, loads existing
     */
    @Serializable
    data class AnimationEditor(val animationId: Long? = null) : Screen()

    /**
     * Preset library screen for browsing saved animations.
     * Per UX-06: Dedicated screen for preset library.
     * @param selectMode If true, selecting a preset returns it; if false, applies to device
     */
    @Serializable
    data class PresetLibrary(val selectMode: Boolean = false) : Screen()
}
