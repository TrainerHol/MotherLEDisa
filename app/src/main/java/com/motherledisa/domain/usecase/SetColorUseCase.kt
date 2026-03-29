package com.motherledisa.domain.usecase

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.motherledisa.data.ble.ConnectedTower
import com.motherledisa.data.ble.TowerConnectionManager
import javax.inject.Inject

/**
 * Use case for setting color on LED towers.
 * Converts Compose Color to RGB values for the protocol.
 */
class SetColorUseCase @Inject constructor(
    private val connectionManager: TowerConnectionManager
) {
    /**
     * Set color on a specific tower.
     */
    operator fun invoke(tower: ConnectedTower, color: Color) {
        val argb = color.toArgb()
        val r = (argb shr 16) and 0xFF
        val g = (argb shr 8) and 0xFF
        val b = argb and 0xFF
        connectionManager.setColor(tower, r, g, b)
    }

    /**
     * Set color on all connected towers (D-12: "All" option).
     */
    fun invokeAll(color: Color) {
        val argb = color.toArgb()
        val r = (argb shr 16) and 0xFF
        val g = (argb shr 8) and 0xFF
        val b = argb and 0xFF
        connectionManager.setColorAll(r, g, b)
    }
}
