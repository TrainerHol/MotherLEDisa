package com.motherledisa.data.ble

/**
 * Represents the current state of a BLE connection attempt or active connection.
 */
sealed class ConnectionState {
    /** No connection attempt in progress */
    data object Idle : ConnectionState()

    /** Currently attempting to connect to a device */
    data class Connecting(val address: String) : ConnectionState()

    /** Successfully connected to a device */
    data class Connected(val address: String) : ConnectionState()

    /** Connection attempt failed or connection was lost */
    data class Error(val message: String, val address: String? = null) : ConnectionState()

    /** Disconnecting from a device */
    data class Disconnecting(val address: String) : ConnectionState()
}
