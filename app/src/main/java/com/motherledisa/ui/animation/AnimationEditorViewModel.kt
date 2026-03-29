package com.motherledisa.ui.animation

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.motherledisa.data.ble.ConnectedTower
import com.motherledisa.data.ble.TowerConnectionManager
import com.motherledisa.domain.animation.AnimationEvaluator
import com.motherledisa.domain.animation.AnimationPlayer
import com.motherledisa.domain.animation.FrameState
import com.motherledisa.domain.model.Animation
import com.motherledisa.domain.model.InterpolationMode
import com.motherledisa.domain.model.Keyframe
import com.motherledisa.domain.model.LoopMode
import com.motherledisa.domain.model.PlaybackState
import com.motherledisa.domain.usecase.LoadPresetsUseCase
import com.motherledisa.domain.usecase.SavePresetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Animation Editor screen.
 * Manages animation state, playback, and keyframe editing.
 */
@HiltViewModel
class AnimationEditorViewModel @Inject constructor(
    private val animationPlayer: AnimationPlayer,
    private val evaluator: AnimationEvaluator,
    private val savePresetUseCase: SavePresetUseCase,
    private val loadPresetsUseCase: LoadPresetsUseCase,
    private val connectionManager: TowerConnectionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Animation ID from navigation (null = new animation)
    private val animationId: Long? = savedStateHandle.get<Long>("animationId")

    // Current animation being edited
    private val _animation = MutableStateFlow(Animation.empty())
    val animation: StateFlow<Animation> = _animation.asStateFlow()

    // Playback state from player
    val playbackState: StateFlow<PlaybackState> = animationPlayer.playbackState

    // Current playhead position (synced with player during playback)
    private val _playheadTimeMs = MutableStateFlow(0L)
    val playheadTimeMs: StateFlow<Long> = _playheadTimeMs.asStateFlow()

    // Current frame for preview (from evaluator or player)
    private val _currentFrame = MutableStateFlow<FrameState?>(null)
    val currentFrame: StateFlow<FrameState?> = _currentFrame.asStateFlow()

    // Selected keyframe for editing
    private val _selectedKeyframe = MutableStateFlow<Keyframe?>(null)
    val selectedKeyframe: StateFlow<Keyframe?> = _selectedKeyframe.asStateFlow()

    // Target device for playback (null = all)
    private val _selectedTowerAddress = MutableStateFlow<String?>(null)
    val selectedTowerAddress: StateFlow<String?> = _selectedTowerAddress.asStateFlow()

    // Connected towers for device picker
    val connectedTowers: StateFlow<List<ConnectedTower>> = connectionManager.connectedTowers

    // Show save dialog
    private val _showSaveDialog = MutableStateFlow(false)
    val showSaveDialog: StateFlow<Boolean> = _showSaveDialog.asStateFlow()

    // Show add keyframe menu
    private val _addKeyframeState = MutableStateFlow<AddKeyframeState?>(null)
    val addKeyframeState: StateFlow<AddKeyframeState?> = _addKeyframeState.asStateFlow()

    init {
        // Load existing animation if editing
        animationId?.let { id ->
            viewModelScope.launch {
                loadPresetsUseCase.getById(id)?.let { loadedAnimation ->
                    _animation.value = loadedAnimation
                }
            }
        }

        // Sync playhead with player during playback
        viewModelScope.launch {
            animationPlayer.currentTimeMs.collect { time ->
                if (playbackState.value == PlaybackState.PLAYING) {
                    _playheadTimeMs.value = time
                }
            }
        }

        // Sync frame state with player during playback
        viewModelScope.launch {
            animationPlayer.currentFrame.collect { frame ->
                if (playbackState.value == PlaybackState.PLAYING) {
                    _currentFrame.value = frame
                }
            }
        }
    }

    // ===== Playback Controls =====

    fun play() {
        val animation = _animation.value
        val tower = _selectedTowerAddress.value?.let { addr ->
            connectionManager.connectedTowers.value.find { it.address == addr }
        }
        animationPlayer.play(animation, tower)
    }

    fun pause() {
        animationPlayer.pause()
    }

    fun resume() {
        animationPlayer.resume()
    }

    fun stop() {
        animationPlayer.stop()
        _playheadTimeMs.value = 0L
        updatePreviewAtTime(0L)
    }

    // ===== Playhead Scrubbing =====

    fun seekTo(timeMs: Long) {
        val animation = _animation.value
        _playheadTimeMs.value = timeMs.coerceIn(0, animation.durationMs)
        updatePreviewAtTime(_playheadTimeMs.value)

        // If playing, seek the player too (with live preview)
        if (playbackState.value != PlaybackState.STOPPED) {
            animationPlayer.seekTo(_playheadTimeMs.value, animation)
        }
    }

    private fun updatePreviewAtTime(timeMs: Long) {
        _currentFrame.value = evaluator.evaluateAt(_animation.value, timeMs)
    }

    // ===== Keyframe Editing =====

    fun selectKeyframe(keyframe: Keyframe) {
        _selectedKeyframe.value = keyframe
    }

    fun deselectKeyframe() {
        _selectedKeyframe.value = null
    }

    fun showAddKeyframeMenu(segment: Int, timeMs: Long) {
        _addKeyframeState.value = AddKeyframeState(segment, timeMs)
    }

    fun dismissAddKeyframeMenu() {
        _addKeyframeState.value = null
    }

    fun addKeyframe(segment: Int, timeMs: Long, color: Color) {
        val newKeyframe = Keyframe(
            timeMs = timeMs,
            segment = segment,
            color = color.toArgb(),
            brightness = 100,
            interpolation = InterpolationMode.SMOOTH
        )

        _animation.update { anim ->
            anim.copy(keyframes = anim.keyframes + newKeyframe)
        }

        _addKeyframeState.value = null
        updatePreviewAtTime(_playheadTimeMs.value)
    }

    fun updateKeyframe(original: Keyframe, updated: Keyframe) {
        _animation.update { anim ->
            anim.copy(
                keyframes = anim.keyframes.map { kf ->
                    if (kf == original) updated else kf
                }
            )
        }
        _selectedKeyframe.value = null
        updatePreviewAtTime(_playheadTimeMs.value)
    }

    fun moveKeyframe(keyframe: Keyframe, newTimeMs: Long) {
        _animation.update { anim ->
            anim.copy(
                keyframes = anim.keyframes.map { kf ->
                    if (kf == keyframe) kf.copy(timeMs = newTimeMs) else kf
                }
            )
        }
        updatePreviewAtTime(_playheadTimeMs.value)
    }

    fun deleteKeyframe(keyframe: Keyframe) {
        _animation.update { anim ->
            anim.copy(keyframes = anim.keyframes - keyframe)
        }
        _selectedKeyframe.value = null
        updatePreviewAtTime(_playheadTimeMs.value)
    }

    // ===== Animation Properties =====

    fun setDuration(durationMs: Long) {
        _animation.update { it.copy(durationMs = durationMs.coerceAtLeast(1000L)) }
        // Clamp playhead to new duration
        if (_playheadTimeMs.value > durationMs) {
            _playheadTimeMs.value = durationMs
        }
    }

    fun setLoopMode(mode: LoopMode) {
        _animation.update { it.copy(loopMode = mode) }
    }

    fun setLoopCount(count: Int) {
        _animation.update { it.copy(loopCount = count.coerceAtLeast(1)) }
    }

    // ===== Device Selection =====

    fun selectTower(address: String?) {
        _selectedTowerAddress.value = address
    }

    // ===== Save =====

    fun showSaveDialog() {
        _showSaveDialog.value = true
    }

    fun dismissSaveDialog() {
        _showSaveDialog.value = false
    }

    fun saveAnimation(name: String) {
        viewModelScope.launch {
            val id = savePresetUseCase(_animation.value, name)
            _animation.update { it.copy(id = id, name = name) }
            _showSaveDialog.value = false
        }
    }
}

/**
 * State for showing add keyframe menu.
 */
data class AddKeyframeState(
    val segment: Int,
    val timeMs: Long
)
