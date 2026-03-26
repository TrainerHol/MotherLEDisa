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
}
