package com.motherledisa.domain.animation

import com.motherledisa.data.ble.ConnectedTower
import com.motherledisa.data.ble.TowerConnectionManager
import com.motherledisa.domain.model.Animation
import com.motherledisa.domain.model.LoopMode
import com.motherledisa.domain.model.PlaybackState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Coroutine-based animation playback engine.
 * Per D-10: Play triggers both preview AND device simultaneously.
 * Per D-12: 30fps (~33ms delay) to prevent BLE queue flooding.
 */
class AnimationPlayer(
    private val connectionManager: TowerConnectionManager,
    private val evaluator: AnimationEvaluator
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _playbackState = MutableStateFlow(PlaybackState.STOPPED)
    /** Current playback state */
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _currentTimeMs = MutableStateFlow(0L)
    /** Current playback position in milliseconds */
    val currentTimeMs: StateFlow<Long> = _currentTimeMs.asStateFlow()

    private val _currentFrame = MutableStateFlow<FrameState?>(null)
    /** Current frame for UI preview */
    val currentFrame: StateFlow<FrameState?> = _currentFrame.asStateFlow()

    private var playbackJob: Job? = null
    private var currentAnimation: Animation? = null
    private var targetTower: ConnectedTower? = null  // null = all towers
    private var remainingLoops: Int = 0
    private var isReversing: Boolean = false  // For ping-pong mode

    /** Frame delay for ~30fps per D-12 */
    val frameDelayMs = 33L

    /**
     * Start playing animation.
     * @param animation The animation to play
     * @param tower Target tower (null = all connected towers)
     */
    fun play(animation: Animation, tower: ConnectedTower? = null) {
        stop()

        currentAnimation = animation
        targetTower = tower
        remainingLoops = animation.loopCount
        isReversing = false

        _playbackState.value = PlaybackState.PLAYING
        _currentTimeMs.value = 0L

        playbackJob = scope.launch {
            runPlaybackLoop(animation)
        }

        Timber.d("Started playback: ${animation.name}, duration=${animation.durationMs}ms, loop=${animation.loopMode}")
    }

    /**
     * Pause playback at current position.
     */
    fun pause() {
        if (_playbackState.value == PlaybackState.PLAYING) {
            playbackJob?.cancel()
            playbackJob = null
            _playbackState.value = PlaybackState.PAUSED
            Timber.d("Paused at ${_currentTimeMs.value}ms")
        }
    }

    /**
     * Resume playback from paused position.
     */
    fun resume() {
        val animation = currentAnimation ?: return
        if (_playbackState.value == PlaybackState.PAUSED) {
            _playbackState.value = PlaybackState.PLAYING
            playbackJob = scope.launch {
                runPlaybackLoop(animation, startTime = _currentTimeMs.value)
            }
            Timber.d("Resumed from ${_currentTimeMs.value}ms")
        }
    }

    /**
     * Stop playback and reset to beginning.
     */
    fun stop() {
        playbackJob?.cancel()
        playbackJob = null
        _playbackState.value = PlaybackState.STOPPED
        _currentTimeMs.value = 0L
        _currentFrame.value = null
        currentAnimation = null
        isReversing = false
        Timber.d("Stopped")
    }

    /**
     * Seek to a specific time (for scrubbing).
     * Only updates preview, does not affect playback state.
     */
    fun seekTo(timeMs: Long, animation: Animation) {
        val clampedTime = timeMs.coerceIn(0, animation.durationMs)
        _currentTimeMs.value = clampedTime
        _currentFrame.value = evaluator.evaluateAt(animation, clampedTime)
    }

    /**
     * Main playback loop running at ~30fps.
     */
    private suspend fun runPlaybackLoop(animation: Animation, startTime: Long = 0L) {
        var currentTime = startTime

        while (currentCoroutineContext().isActive && _playbackState.value == PlaybackState.PLAYING) {
            // Evaluate frame
            val frame = evaluator.evaluateAt(animation, currentTime)
            _currentFrame.value = frame
            _currentTimeMs.value = currentTime

            // Send to device(s) per D-10
            sendFrameToDevice(frame)

            // Advance time
            delay(frameDelayMs)
            currentTime += if (isReversing) -frameDelayMs else frameDelayMs

            // Handle loop boundaries
            when {
                !isReversing && currentTime >= animation.durationMs -> {
                    handleLoopEnd(animation) { currentTime = it }
                }
                isReversing && currentTime <= 0 -> {
                    handlePingPongReturn(animation) { currentTime = it }
                }
            }
        }
    }

    /**
     * Handle reaching end of animation.
     */
    private fun handleLoopEnd(animation: Animation, setTime: (Long) -> Unit) {
        when (animation.loopMode) {
            LoopMode.ONCE -> {
                stop()
            }
            LoopMode.COUNT -> {
                remainingLoops--
                if (remainingLoops <= 0) {
                    stop()
                } else {
                    setTime(0L)
                }
            }
            LoopMode.INFINITE -> {
                setTime(0L)
            }
            LoopMode.PING_PONG -> {
                isReversing = true
                setTime(animation.durationMs)
            }
        }
    }

    /**
     * Handle reaching start during ping-pong reverse.
     */
    private fun handlePingPongReturn(animation: Animation, setTime: (Long) -> Unit) {
        remainingLoops--
        if (animation.loopMode == LoopMode.PING_PONG && remainingLoops > 0) {
            isReversing = false
            setTime(0L)
        } else if (animation.loopMode == LoopMode.PING_PONG) {
            // Ping-pong with no loop count = infinite
            isReversing = false
            setTime(0L)
        } else {
            stop()
        }
    }

    /**
     * Send frame colors and brightness to connected tower(s).
     */
    private fun sendFrameToDevice(frame: FrameState) {
        // For now, send average/dominant color to device
        // Per RESEARCH.md Open Question 1: Protocol may not support per-segment
        // Use segment 0 (base) color as primary
        val primaryColor = frame.segmentColors[0] ?: return
        val primaryBrightness = frame.segmentBrightness[0] ?: 100

        val r = (primaryColor shr 16) and 0xFF
        val g = (primaryColor shr 8) and 0xFF
        val b = primaryColor and 0xFF

        val tower = targetTower
        if (tower != null) {
            connectionManager.setColor(tower, r, g, b)
            connectionManager.setBrightness(tower, primaryBrightness)
        } else {
            connectionManager.setColorAll(r, g, b)
            connectionManager.setBrightnessAll(primaryBrightness)
        }
    }
}
