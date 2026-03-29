package com.motherledisa.domain.orchestration

import com.motherledisa.data.ble.ConnectedTower
import com.motherledisa.data.ble.TowerConnectionManager
import com.motherledisa.domain.animation.AnimationPlayer
import com.motherledisa.domain.model.Animation
import com.motherledisa.domain.model.PlaybackState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Orchestrates animations across multiple towers with timing modes.
 * Per D-04: Mode is global across all towers.
 * Per D-08: Accept 20-50ms BLE latency variance.
 */
@Singleton
class OrchestrationManager @Inject constructor(
    private val connectionManager: TowerConnectionManager,
    private val animationPlayer: AnimationPlayer
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _orchestrationMode = MutableStateFlow(OrchestrationMode.MIRROR)
    /** Current orchestration mode */
    val orchestrationMode: StateFlow<OrchestrationMode> = _orchestrationMode.asStateFlow()

    private val _offsetDelayMs = MutableStateFlow(500)
    /** Delay between consecutive tower starts in offset mode (D-06: 0-2000ms) */
    val offsetDelayMs: StateFlow<Int> = _offsetDelayMs.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    /** True when orchestrated playback is active */
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    /**
     * Sets the orchestration mode.
     */
    fun setMode(mode: OrchestrationMode) {
        _orchestrationMode.value = mode
        Timber.d("Orchestration mode changed to: ${mode.displayName}")
    }

    /**
     * Sets the offset delay for OFFSET mode.
     * @param delayMs Delay in milliseconds (0-2000 per D-06)
     */
    fun setOffsetDelay(delayMs: Int) {
        _offsetDelayMs.value = delayMs.coerceIn(0, 2000)
        Timber.d("Offset delay set to: ${delayMs}ms")
    }

    /**
     * Plays animation across ordered towers using current orchestration mode.
     * @param animation The animation to play
     * @param orderedTowers Towers in user-defined order (per D-01/D-02)
     */
    fun playOrchestrated(animation: Animation, orderedTowers: List<ConnectedTower>) {
        if (orderedTowers.isEmpty()) {
            Timber.w("No towers to orchestrate")
            return
        }

        stopOrchestrated()
        _isPlaying.value = true

        when (_orchestrationMode.value) {
            OrchestrationMode.MIRROR -> playMirror(animation, orderedTowers)
            OrchestrationMode.OFFSET -> playOffset(animation, orderedTowers)
            OrchestrationMode.CASCADE -> playCascade(animation, orderedTowers)
            OrchestrationMode.INDEPENDENT -> {
                // Independent mode is handled by ViewModel with per-tower assignments
                Timber.d("Independent mode - handled by ViewModel")
            }
        }
    }

    /**
     * Stops all orchestrated playback.
     */
    fun stopOrchestrated() {
        animationPlayer.stop()
        _isPlaying.value = false
        Timber.d("Orchestrated playback stopped")
    }

    /**
     * Mirror mode: All towers play same animation simultaneously.
     * Per MULTI-02: All towers show same animation.
     */
    private fun playMirror(animation: Animation, towers: List<ConnectedTower>) {
        scope.launch {
            Timber.d("Starting mirror playback on ${towers.size} towers")
            // Small stagger (10ms) between tower commands to prevent BLE flooding
            // Per D-08: Accept variance, just avoid simultaneous flooding
            towers.forEachIndexed { index, tower ->
                if (index > 0) delay(10)
                animationPlayer.play(animation, tower)
            }
        }
    }

    /**
     * Offset mode: Staggered start times per D-06.
     * Per MULTI-03: Staggered timing across towers.
     */
    private fun playOffset(animation: Animation, towers: List<ConnectedTower>) {
        scope.launch {
            val delayMs = _offsetDelayMs.value.toLong()
            Timber.d("Starting offset playback: ${towers.size} towers, ${delayMs}ms delay")

            towers.forEachIndexed { index, tower ->
                delay(index * delayMs)
                animationPlayer.play(animation, tower)
            }
        }
    }

    /**
     * Cascade mode: Sequential relay per D-07.
     * Per MULTI-04: Tower-to-tower relay.
     * Per D-07: Immediate handoff - next tower starts when previous completes.
     */
    private fun playCascade(animation: Animation, towers: List<ConnectedTower>) {
        scope.launch {
            Timber.d("Starting cascade playback on ${towers.size} towers")

            for (tower in towers) {
                // Start animation on this tower
                animationPlayer.play(animation, tower)

                // Wait for this tower's animation to complete
                // Per D-07: Immediate handoff when STOPPED
                animationPlayer.playbackState
                    .filter { it == PlaybackState.STOPPED }
                    .first()

                Timber.d("Tower ${tower.name} completed, starting next")
            }

            _isPlaying.value = false
            Timber.d("Cascade playback complete")
        }
    }
}
