package com.motherledisa.ui.orchestrate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.motherledisa.data.ble.ConnectedTower
import com.motherledisa.data.ble.TowerConnectionManager
import com.motherledisa.data.local.TowerConfigDao
import com.motherledisa.data.repository.AnimationRepository
import com.motherledisa.domain.model.Animation
import com.motherledisa.domain.orchestration.OrchestrationManager
import com.motherledisa.domain.orchestration.OrchestrationMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for OrchestrateScreen.
 * Manages orchestration mode, tower ordering, and per-tower animation assignments.
 * Per D-02: Global default tower order saved in app preferences.
 */
@HiltViewModel
class OrchestrateViewModel @Inject constructor(
    private val connectionManager: TowerConnectionManager,
    private val orchestrationManager: OrchestrationManager,
    private val towerConfigDao: TowerConfigDao,
    private val animationRepository: AnimationRepository
) : ViewModel() {

    /**
     * Connected towers sorted by user-defined position.
     * Per D-01: Drag-and-drop vertical list for tower ordering.
     * Per MULTI-05: User can define tower ordering for offset/cascade modes.
     */
    val orderedTowers: StateFlow<List<ConnectedTower>> = combine(
        connectionManager.connectedTowers,
        towerConfigDao.getOrderedDevices()
    ) { connected, ordered ->
        // Sort connected towers by their saved position
        connected.sortedBy { tower ->
            ordered.find { it.address == tower.address }?.position ?: Int.MAX_VALUE
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Current orchestration mode */
    val orchestrationMode: StateFlow<OrchestrationMode> = orchestrationManager.orchestrationMode

    /** Offset delay in milliseconds */
    val offsetDelayMs: StateFlow<Int> = orchestrationManager.offsetDelayMs

    /** Whether orchestrated playback is active */
    val isPlaying: StateFlow<Boolean> = orchestrationManager.isPlaying

    /** Independent mode: animation ID per tower address */
    private val _towerAnimations = MutableStateFlow<Map<String, Long>>(emptyMap())
    val towerAnimations: StateFlow<Map<String, Long>> = _towerAnimations.asStateFlow()

    /** Available animations for independent mode dropdown */
    val animations: StateFlow<List<Animation>> = animationRepository
        .getAllAnimations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Currently selected animation for non-independent modes */
    private val _selectedAnimation = MutableStateFlow<Animation?>(null)
    val selectedAnimation: StateFlow<Animation?> = _selectedAnimation.asStateFlow()

    /**
     * Sets the orchestration mode.
     * Per D-03: Horizontal segmented control for mode switching.
     * Per D-04: Mode selection is global across all towers.
     */
    fun setMode(mode: OrchestrationMode) {
        orchestrationManager.setMode(mode)
    }

    /**
     * Sets the offset delay for OFFSET mode.
     * Per D-06: Single delay slider (0-2000ms range).
     */
    fun setOffsetDelay(delayMs: Int) {
        orchestrationManager.setOffsetDelay(delayMs)
    }

    /**
     * Reorders towers by moving item from fromIndex to toIndex.
     * Per D-01: Drag-and-drop vertical list for tower ordering.
     * Per D-02: Global default tower order saved in app preferences.
     */
    fun reorderTowers(fromIndex: Int, toIndex: Int) {
        viewModelScope.launch {
            val towers = orderedTowers.value.toMutableList()
            if (fromIndex !in towers.indices || toIndex !in towers.indices) {
                Timber.w("Invalid reorder indices: $fromIndex -> $toIndex")
                return@launch
            }

            // Move item
            val item = towers.removeAt(fromIndex)
            towers.add(toIndex, item)

            // Persist new positions
            towers.forEachIndexed { index, tower ->
                towerConfigDao.updatePosition(tower.address, index)
            }

            Timber.d("Reordered towers: ${towers.map { it.name }}")
        }
    }

    /**
     * Sets animation for a specific tower in independent mode.
     * Per D-05: Dropdown per tower to select animation/preset.
     * Per MULTI-06: User can enable independent mode (each tower controlled separately).
     */
    fun setTowerAnimation(towerAddress: String, animationId: Long?) {
        _towerAnimations.update { current ->
            if (animationId != null) {
                current + (towerAddress to animationId)
            } else {
                current - towerAddress
            }
        }
        Timber.d("Tower $towerAddress animation set to: $animationId")
    }

    /**
     * Sets the selected animation for non-independent modes.
     */
    fun selectAnimation(animation: Animation?) {
        _selectedAnimation.value = animation
    }

    /**
     * Starts orchestrated playback with current settings.
     */
    fun playOrchestrated() {
        val animation = _selectedAnimation.value
        if (animation == null) {
            Timber.w("No animation selected for playback")
            return
        }

        val towers = orderedTowers.value
        if (towers.size < 2) {
            Timber.w("Need at least 2 towers for orchestration")
            return
        }

        orchestrationManager.playOrchestrated(animation, towers)
    }

    /**
     * Plays independent animations for each tower.
     * Per MULTI-06: Each tower controlled separately.
     */
    fun playIndependent() {
        viewModelScope.launch {
            val towers = orderedTowers.value
            val animationMap = _towerAnimations.value

            towers.forEach { tower ->
                val animationId = animationMap[tower.address] ?: return@forEach
                val animation = animationRepository.getById(animationId)
                if (animation != null) {
                    orchestrationManager.playOrchestrated(animation, listOf(tower))
                    Timber.d("Playing ${animation.name} on ${tower.name}")
                } else {
                    Timber.w("Animation $animationId not found for tower ${tower.name}")
                }
            }
        }
    }

    /**
     * Stops all playback.
     */
    fun stopPlayback() {
        orchestrationManager.stopOrchestrated()
    }
}
