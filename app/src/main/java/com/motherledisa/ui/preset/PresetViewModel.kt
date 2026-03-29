package com.motherledisa.ui.preset

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.motherledisa.data.ble.ConnectedTower
import com.motherledisa.data.ble.TowerConnectionManager
import com.motherledisa.domain.animation.AnimationPlayer
import com.motherledisa.domain.model.Animation
import com.motherledisa.domain.model.PlaybackState
import com.motherledisa.domain.usecase.DeletePresetUseCase
import com.motherledisa.domain.usecase.LoadPresetsUseCase
import com.motherledisa.domain.usecase.PlayAnimationUseCase
import com.motherledisa.domain.usecase.SavePresetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Preset Library screen.
 * Per PRESET-02: View list of saved presets.
 */
@HiltViewModel
class PresetViewModel @Inject constructor(
    private val loadPresetsUseCase: LoadPresetsUseCase,
    private val playAnimationUseCase: PlayAnimationUseCase,
    private val deletePresetUseCase: DeletePresetUseCase,
    private val savePresetUseCase: SavePresetUseCase,
    private val animationPlayer: AnimationPlayer,
    private val connectionManager: TowerConnectionManager
) : ViewModel() {

    /** All saved presets */
    val presets: StateFlow<List<Animation>> = loadPresetsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Current playback state */
    val playbackState: StateFlow<PlaybackState> = animationPlayer.playbackState

    /** Currently playing animation ID */
    private val _playingAnimationId = MutableStateFlow<Long?>(null)
    val playingAnimationId: StateFlow<Long?> = _playingAnimationId.asStateFlow()

    /** Animation with open options menu */
    private val _selectedAnimation = MutableStateFlow<Animation?>(null)
    val selectedAnimation: StateFlow<Animation?> = _selectedAnimation.asStateFlow()

    /** Selected tower for playback */
    private val _selectedTowerAddress = MutableStateFlow<String?>(null)
    val selectedTowerAddress: StateFlow<String?> = _selectedTowerAddress.asStateFlow()

    /** Connected towers for device picker */
    val connectedTowers: StateFlow<List<ConnectedTower>> = connectionManager.connectedTowers

    init {
        // Track when playback stops
        viewModelScope.launch {
            playbackState.collect { state ->
                if (state == PlaybackState.STOPPED) {
                    _playingAnimationId.value = null
                }
            }
        }
    }

    /**
     * Preview animation (tap).
     * Per D-14: Single tap to preview.
     */
    fun previewAnimation(animation: Animation) {
        // If already playing this animation, stop it
        if (_playingAnimationId.value == animation.id) {
            animationPlayer.stop()
            _playingAnimationId.value = null
            return
        }

        // Play on selected device(s)
        val tower = _selectedTowerAddress.value?.let { addr ->
            connectionManager.connectedTowers.value.find { it.address == addr }
        }

        _playingAnimationId.value = animation.id

        if (tower != null) {
            playAnimationUseCase(animation, tower)
        } else {
            playAnimationUseCase.invokeAll(animation)
        }
    }

    /**
     * Apply animation to device (from options menu).
     * Per D-16: "Apply" sends preset to currently selected device(s).
     */
    fun applyAnimation(animation: Animation) {
        previewAnimation(animation)
    }

    /**
     * Show options menu for animation.
     * Per D-14: Long-press for options.
     */
    fun showOptions(animation: Animation) {
        _selectedAnimation.value = animation
    }

    /**
     * Dismiss options menu.
     */
    fun dismissOptions() {
        _selectedAnimation.value = null
    }

    /**
     * Rename preset.
     */
    fun renameAnimation(animation: Animation, newName: String) {
        viewModelScope.launch {
            savePresetUseCase(animation.copy(name = newName))
        }
    }

    /**
     * Duplicate preset.
     */
    fun duplicateAnimation(animation: Animation) {
        viewModelScope.launch {
            val copy = animation.copy(
                id = 0,  // New ID
                name = "${animation.name} (Copy)",
                createdAt = System.currentTimeMillis()
            )
            savePresetUseCase(copy)
        }
    }

    /**
     * Delete preset.
     * Per PRESET-04: User can delete saved presets.
     */
    fun deleteAnimation(animation: Animation) {
        viewModelScope.launch {
            // Stop if currently playing
            if (_playingAnimationId.value == animation.id) {
                animationPlayer.stop()
            }
            deletePresetUseCase(animation)
        }
    }

    /**
     * Stop current playback.
     */
    fun stopPlayback() {
        animationPlayer.stop()
        _playingAnimationId.value = null
    }

    /**
     * Select target tower for playback.
     */
    fun selectTower(address: String?) {
        _selectedTowerAddress.value = address
    }
}
