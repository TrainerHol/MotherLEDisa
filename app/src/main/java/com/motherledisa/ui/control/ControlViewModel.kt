package com.motherledisa.ui.control

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.motherledisa.data.ble.ConnectedTower
import com.motherledisa.data.ble.TowerConnectionManager
import com.motherledisa.domain.model.AllEffects
import com.motherledisa.domain.model.Effect
import com.motherledisa.domain.model.EffectCategory
import com.motherledisa.domain.model.TowerState
import com.motherledisa.domain.usecase.SetBrightnessUseCase
import com.motherledisa.domain.usecase.SetColorUseCase
import com.motherledisa.domain.usecase.SetEffectUseCase
import com.motherledisa.domain.usecase.TogglePowerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * ViewModel for the Control Screen.
 *
 * Manages tower state and handles debounced BLE commands per D-10.
 * UI updates immediately for responsive preview, while BLE commands
 * are debounced to prevent queue flooding.
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class ControlViewModel @Inject constructor(
    private val connectionManager: TowerConnectionManager,
    private val togglePowerUseCase: TogglePowerUseCase,
    private val setColorUseCase: SetColorUseCase,
    private val setBrightnessUseCase: SetBrightnessUseCase,
    private val setEffectUseCase: SetEffectUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Initial device address from navigation (may be null for "All devices" mode)
    private val initialDeviceAddress: String? = savedStateHandle.get<String>("deviceAddress")

    /** Flow of currently connected towers */
    val connectedTowers: StateFlow<List<ConnectedTower>> = connectionManager.connectedTowers

    // Currently selected device (null = all devices per D-12)
    private val _selectedTowerAddress = MutableStateFlow<String?>(initialDeviceAddress)
    val selectedTowerAddress: StateFlow<String?> = _selectedTowerAddress.asStateFlow()

    // UI state (updates immediately for preview)
    private val _towerState = MutableStateFlow(TowerState())
    val towerState: StateFlow<TowerState> = _towerState.asStateFlow()

    // Debounced brightness (D-10: ~30fps)
    private val _brightness = MutableStateFlow(100)

    // Debounced color
    private val _color = MutableStateFlow(Color.White)

    init {
        // D-10: Debounce brightness commands to 30fps (33ms)
        _brightness
            .debounce(33)  // ~30fps
            .distinctUntilChanged()
            .onEach { brightness ->
                sendBrightness(brightness)
            }
            .launchIn(viewModelScope)

        // Debounce color to prevent flooding (slightly slower than brightness)
        _color
            .debounce(50)
            .distinctUntilChanged()
            .onEach { color ->
                sendColor(color)
            }
            .launchIn(viewModelScope)
    }

    /** All available effects */
    val availableEffects: List<Effect> = AllEffects.all

    /** Effects grouped by category (D-22: section headers) */
    val effectsByCategory: Map<EffectCategory, List<Effect>> = AllEffects.byCategory

    /**
     * Select a specific tower or all towers (null).
     */
    fun selectTower(address: String?) {
        _selectedTowerAddress.value = address
    }

    /**
     * Toggle power on/off for selected tower(s).
     */
    fun togglePower() {
        val newPowerState = !_towerState.value.isPoweredOn
        _towerState.update { it.copy(isPoweredOn = newPowerState) }

        val address = _selectedTowerAddress.value
        if (address == null) {
            togglePowerUseCase.invokeAll(newPowerState)
        } else {
            connectionManager.connectedTowers.value
                .find { it.address == address }
                ?.let { togglePowerUseCase(it, newPowerState) }
        }
    }

    /**
     * Set color for selected tower(s).
     * UI updates immediately; BLE command is debounced.
     */
    fun setColor(color: Color) {
        // Update UI immediately (preview updates at 60fps)
        _towerState.update {
            it.copy(
                currentColor = color,
                segmentColors = (0 until it.segmentCount).associateWith { color }
            )
        }
        // Debounced BLE command
        _color.value = color
    }

    private fun sendColor(color: Color) {
        val address = _selectedTowerAddress.value
        if (address == null) {
            setColorUseCase.invokeAll(color)
        } else {
            connectionManager.connectedTowers.value
                .find { it.address == address }
                ?.let { setColorUseCase(it, color) }
        }
    }

    /**
     * Set brightness for selected tower(s).
     * UI updates immediately; BLE command is debounced to ~30fps.
     */
    fun setBrightness(brightness: Int) {
        // Update UI immediately
        _towerState.update { it.copy(brightness = brightness) }
        // Debounced BLE command
        _brightness.value = brightness
    }

    private fun sendBrightness(brightness: Int) {
        val address = _selectedTowerAddress.value
        if (address == null) {
            setBrightnessUseCase.invokeAll(brightness)
        } else {
            connectionManager.connectedTowers.value
                .find { it.address == address }
                ?.let { setBrightnessUseCase(it, brightness) }
        }
    }

    /**
     * Set active effect for selected tower(s).
     */
    fun setEffect(effect: Effect) {
        _towerState.update { it.copy(activeEffect = effect) }

        val speed = _towerState.value.effectSpeed
        val address = _selectedTowerAddress.value
        if (address == null) {
            setEffectUseCase.invokeAll(effect, speed)
        } else {
            connectionManager.connectedTowers.value
                .find { it.address == address }
                ?.let { setEffectUseCase(it, effect, speed) }
        }
    }

    /**
     * Set effect playback speed.
     */
    fun setEffectSpeed(speed: Int) {
        _towerState.update { it.copy(effectSpeed = speed) }

        val activeEffect = _towerState.value.activeEffect ?: return
        val address = _selectedTowerAddress.value
        if (address == null) {
            setEffectUseCase.invokeAll(activeEffect, speed)
        } else {
            connectionManager.connectedTowers.value
                .find { it.address == address }
                ?.let { setEffectUseCase(it, activeEffect, speed) }
        }
    }

    /**
     * Clear active effect (return to static color mode).
     */
    fun clearEffect() {
        _towerState.update { it.copy(activeEffect = null) }
    }
}
