package com.motherledisa.domain.usecase

import com.motherledisa.data.ble.ConnectedTower
import com.motherledisa.data.ble.TowerConnectionManager
import javax.inject.Inject

/**
 * Use case for toggling power state on LED towers.
 * Supports individual tower control or all-towers mode (D-12: "All" option).
 */
class TogglePowerUseCase @Inject constructor(
    private val connectionManager: TowerConnectionManager
) {
    /**
     * Toggle power on a specific tower.
     */
    operator fun invoke(tower: ConnectedTower, powerOn: Boolean) {
        connectionManager.setPower(tower, powerOn)
    }

    /**
     * Toggle power on all connected towers (D-12: "All" option).
     */
    fun invokeAll(powerOn: Boolean) {
        connectionManager.setPowerAll(powerOn)
    }
}
