package com.motherledisa.domain.usecase

import com.motherledisa.data.ble.ConnectedTower
import com.motherledisa.data.ble.TowerConnectionManager
import com.motherledisa.domain.model.Effect
import javax.inject.Inject

/**
 * Use case for setting hardware effects on LED towers.
 * Effects are built-in patterns from the ELK-BLEDOM protocol.
 */
class SetEffectUseCase @Inject constructor(
    private val connectionManager: TowerConnectionManager
) {
    /**
     * Set effect on a specific tower.
     * @param effect Effect to activate
     * @param speed Effect playback speed (0-100)
     */
    operator fun invoke(tower: ConnectedTower, effect: Effect, speed: Int) {
        connectionManager.setEffect(tower, effect.id, speed.coerceIn(0, 100))
    }

    /**
     * Set effect on all connected towers (D-12: "All" option).
     * @param effect Effect to activate
     * @param speed Effect playback speed (0-100)
     */
    fun invokeAll(effect: Effect, speed: Int) {
        connectionManager.setEffectAll(effect.id, speed.coerceIn(0, 100))
    }
}
