package com.motherledisa.domain.model

/**
 * Sealed class representing a device in the device list.
 * Merges scan results with known (previously connected) devices per D-05.
 */
sealed class DeviceListItem {
    /** MAC address - unique identifier */
    abstract val address: String
    /** Display name (custom name if set, otherwise device name) */
    abstract val displayName: String
    /** Signal strength in dBm, null if device not currently visible */
    abstract val rssi: Int?
    /** Whether device is currently connected */
    abstract val isConnected: Boolean
    /** Timestamp of last successful connection */
    abstract val lastConnected: Long?

    /**
     * Device currently visible via BLE scan.
     */
    data class Available(
        override val address: String,
        override val displayName: String,
        override val rssi: Int,
        override val isConnected: Boolean,
        override val lastConnected: Long?,
        /** Whether this device was previously connected (known device) */
        val isKnown: Boolean
    ) : DeviceListItem()

    /**
     * Known device not currently visible via scan.
     * Shows "Last connected" badge per D-05.
     */
    data class Known(
        override val address: String,
        override val displayName: String,
        override val rssi: Int? = null,
        override val isConnected: Boolean = false,
        override val lastConnected: Long?
    ) : DeviceListItem()
}
