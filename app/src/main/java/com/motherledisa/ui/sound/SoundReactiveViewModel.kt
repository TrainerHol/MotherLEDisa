package com.motherledisa.ui.sound

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.motherledisa.data.ble.ConnectedTower
import com.motherledisa.data.ble.TowerConnectionManager
import com.motherledisa.data.repository.AnimationRepository
import com.motherledisa.domain.model.Animation
import com.motherledisa.domain.model.SoundEffect
import com.motherledisa.domain.model.SoundPalette
import com.motherledisa.domain.usecase.DisableSoundModeUseCase
import com.motherledisa.domain.usecase.EnableSoundModeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import timber.log.Timber
import javax.inject.Inject

/**
 * Sound-reactive screen UI state.
 */
data class SoundReactiveUiState(
    val isSoundModeEnabled: Boolean = false,
    val selectedEffect: SoundEffect = SoundEffect.ENERGETIC,
    val sensitivity: Int = 50,
    val palette: SoundPalette = SoundPalette.DEFAULT,
    val isColorPickerVisible: Boolean = false
)

/**
 * ViewModel for SoundReactiveScreen.
 * Per D-02: App sends configuration to tower; tower runs autonomously.
 * Per D-03: Fire-and-forget - app doesn't need to stay connected.
 */
@HiltViewModel
class SoundReactiveViewModel @Inject constructor(
    private val connectionManager: TowerConnectionManager,
    private val animationRepository: AnimationRepository,
    private val enableSoundModeUseCase: EnableSoundModeUseCase,
    private val disableSoundModeUseCase: DisableSoundModeUseCase
) : ViewModel() {

    /** Connected towers for device picker */
    val connectedTowers: StateFlow<List<ConnectedTower>> = connectionManager.connectedTowers

    /** Currently selected tower (null = all towers) */
    private val _selectedTowerAddress = MutableStateFlow<String?>(null)
    val selectedTowerAddress: StateFlow<String?> = _selectedTowerAddress.asStateFlow()

    /** UI state */
    private val _uiState = MutableStateFlow(SoundReactiveUiState())
    val uiState: StateFlow<SoundReactiveUiState> = _uiState.asStateFlow()

    /** Saved animations for palette extraction (per D-05) */
    val savedAnimations: StateFlow<List<Animation>> = animationRepository
        .getAllAnimations()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Select target tower for sound mode commands.
     * @param address Tower address, or null for all towers
     */
    fun selectTower(address: String?) {
        _selectedTowerAddress.value = address
    }

    /**
     * Toggle sound-reactive mode on/off.
     * When enabling: sends effect, sensitivity, palette to tower then enables mic.
     * When disabling: sends disableMic command.
     * Per pitfall #2: Must disable mic before switching to manual mode.
     */
    fun toggleSoundMode(enabled: Boolean) {
        Timber.d("Toggle sound mode: $enabled")
        _uiState.update { it.copy(isSoundModeEnabled = enabled) }

        if (enabled) {
            applySoundMode()
        } else {
            disableSoundMode()
        }
    }

    /**
     * Select sound-reactive effect.
     * If sound mode is enabled, immediately applies to tower.
     */
    fun selectEffect(effect: SoundEffect) {
        Timber.d("Select effect: ${effect.displayName}")
        _uiState.update { it.copy(selectedEffect = effect) }

        if (_uiState.value.isSoundModeEnabled) {
            applySoundMode()
        }
    }

    /**
     * Update sensitivity value.
     * If sound mode is enabled, immediately applies to tower.
     */
    fun setSensitivity(sensitivity: Int) {
        _uiState.update { it.copy(sensitivity = sensitivity) }

        if (_uiState.value.isSoundModeEnabled) {
            applySoundMode()
        }
    }

    /**
     * Update color palette.
     * If sound mode is enabled, immediately applies to tower (using primary color).
     */
    fun setPalette(palette: SoundPalette) {
        Timber.d("Set palette: ${palette.colors.size} colors, primary=${palette.primaryIndex}")
        _uiState.update { it.copy(palette = palette) }

        if (_uiState.value.isSoundModeEnabled) {
            applySoundMode()
        }
    }

    /**
     * Add color to palette.
     */
    fun addColorToPalette(color: Color) {
        val currentPalette = _uiState.value.palette
        if (currentPalette.colors.size < 5) {
            val newColors = currentPalette.colors + color
            setPalette(SoundPalette(newColors, currentPalette.primaryIndex))
        }
    }

    /**
     * Show/hide color picker dialog.
     */
    fun setColorPickerVisible(visible: Boolean) {
        _uiState.update { it.copy(isColorPickerVisible = visible) }
    }

    /**
     * Apply current sound mode settings to tower(s).
     * Command sequence: setColor -> setMicEffect -> setMicSensitivity -> enableMic
     */
    private fun applySoundMode() {
        val state = _uiState.value
        val address = _selectedTowerAddress.value

        if (address == null) {
            enableSoundModeUseCase.invokeAll(
                effect = state.selectedEffect,
                sensitivity = state.sensitivity,
                baseColor = state.palette.primaryColor
            )
        } else {
            connectionManager.connectedTowers.value
                .find { it.address == address }
                ?.let { tower ->
                    enableSoundModeUseCase(
                        tower = tower,
                        effect = state.selectedEffect,
                        sensitivity = state.sensitivity,
                        baseColor = state.palette.primaryColor
                    )
                }
        }
    }

    /**
     * Disable sound mode on tower(s).
     */
    private fun disableSoundMode() {
        val address = _selectedTowerAddress.value

        if (address == null) {
            disableSoundModeUseCase.invokeAll()
        } else {
            connectionManager.connectedTowers.value
                .find { it.address == address }
                ?.let { tower ->
                    disableSoundModeUseCase(tower)
                }
        }
    }
}
