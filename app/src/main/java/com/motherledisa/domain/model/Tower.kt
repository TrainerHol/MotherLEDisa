package com.motherledisa.domain.model

/**
 * Domain model representing an LED tower device.
 * Contains immutable device identification and connection metadata.
 */
data class Tower(
    /** MAC address - unique device identifier */
    val address: String,
    /** Device name from BLE advertisement (e.g., "MELK-12AB34") */
    val name: String,
    /** User-assigned custom name (D-16: e.g., "Desk Tower") */
    val customName: String? = null,
    /** Whether device is currently connected */
    val isConnected: Boolean = false,
    /** Timestamp of last successful connection (for "Last connected" badge - D-05) */
    val lastConnected: Long? = null
) {
    /** Display name prioritizes custom name if set, otherwise device name */
    val displayName: String
        get() = customName ?: name
}
