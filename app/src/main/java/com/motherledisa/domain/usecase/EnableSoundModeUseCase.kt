package com.motherledisa.domain.usecase

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.motherledisa.data.ble.ConnectedTower
import com.motherledisa.data.ble.TowerConnectionManager
import com.motherledisa.domain.model.SoundEffect
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enables sound-reactive mode on tower's built-in microphone.
 * Per D-02: App sends configuration, tower runs autonomously.
 * Per D-03: Fire-and-forget - app doesn't need to stay connected.
 *
 * Command sequence:
 * 1. Set base color (tower uses this for effect)
 * 2. Set effect type (0x80-0x87)
 * 3. Set sensitivity (0-100)
 * 4. Enable microphone
 */
@Singleton
class EnableSoundModeUseCase @Inject constructor(
    private val connectionManager: TowerConnectionManager
) {
    /**
     * Enable sound mode on single tower.
     * @param tower Target tower
     * @param effect Sound-reactive effect (0x80-0x87)
     * @param sensitivity Mic sensitivity 0-100
     * @param baseColor Base color for effect (tower uses as starting point)
     */
    operator fun invoke(
        tower: ConnectedTower,
        effect: SoundEffect,
        sensitivity: Int,
        baseColor: Color
    ) {
        Timber.d("Enabling sound mode on ${tower.name}: effect=${effect.displayName}, sensitivity=$sensitivity")

        val argb = baseColor.toArgb()
        val r = (argb shr 16) and 0xFF
        val g = (argb shr 8) and 0xFF
        val b = argb and 0xFF

        // Set base color first (tower uses this for effect) - per Research pitfall #1
        connectionManager.setColor(tower, r, g, b)
        // Set effect
        connectionManager.setMicEffect(tower, effect.id)
        // Set sensitivity
        connectionManager.setMicSensitivity(tower, sensitivity)
        // Enable microphone last
        connectionManager.enableMic(tower)
    }

    /**
     * Enable sound mode on all connected towers.
     */
    fun invokeAll(effect: SoundEffect, sensitivity: Int, baseColor: Color) {
        connectionManager.connectedTowers.value.forEach { tower ->
            invoke(tower, effect, sensitivity, baseColor)
        }
    }
}
