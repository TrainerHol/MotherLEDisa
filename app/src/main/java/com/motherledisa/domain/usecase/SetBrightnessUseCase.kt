package com.motherledisa.domain.usecase

import com.motherledisa.data.ble.ConnectedTower
import com.motherledisa.data.ble.TowerConnectionManager
import javax.inject.Inject

/**
 * Use case for setting brightness on LED towers.
 * Brightness is clamped to 0-100 range per protocol.
 */
class SetBrightnessUseCase @Inject constructor(
    private val connectionManager: TowerConnectionManager
) {
    /**
     * Set brightness on a specific tower.
     * @param brightness Brightness level (0-100)
     */
    operator fun invoke(tower: ConnectedTower, brightness: Int) {
        connectionManager.setBrightness(tower, brightness.coerceIn(0, 100))
    }

    /**
     * Set brightness on all connected towers (D-12: "All" option).
     * @param brightness Brightness level (0-100)
     */
    fun invokeAll(brightness: Int) {
        connectionManager.setBrightnessAll(brightness.coerceIn(0, 100))
    }
}
