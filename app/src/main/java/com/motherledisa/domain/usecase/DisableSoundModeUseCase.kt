package com.motherledisa.domain.usecase

import com.motherledisa.data.ble.ConnectedTower
import com.motherledisa.data.ble.TowerConnectionManager
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Disables sound-reactive mode on tower.
 * Per Research pitfall #2: Must disable mic before switching to manual mode.
 */
@Singleton
class DisableSoundModeUseCase @Inject constructor(
    private val connectionManager: TowerConnectionManager
) {
    /**
     * Disable sound mode on single tower.
     */
    operator fun invoke(tower: ConnectedTower) {
        Timber.d("Disabling sound mode on ${tower.name}")
        connectionManager.disableMic(tower)
    }

    /**
     * Disable sound mode on all connected towers.
     */
    fun invokeAll() {
        connectionManager.connectedTowers.value.forEach { tower ->
            invoke(tower)
        }
    }
}
