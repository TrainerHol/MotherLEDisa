package com.motherledisa.ui.orchestrate

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DevicesOther
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.motherledisa.domain.model.Animation
import com.motherledisa.domain.orchestration.OrchestrationMode
import com.motherledisa.ui.orchestrate.components.IndependentTowerConfigList
import com.motherledisa.ui.orchestrate.components.OffsetDelaySlider
import com.motherledisa.ui.orchestrate.components.OrchestrationModeSelector
import com.motherledisa.ui.orchestrate.components.TowerOrderList

/**
 * Multi-tower orchestration screen.
 * Per D-09: Dedicated "Orchestrate" tab in bottom navigation.
 * Per D-11: Visible but disabled when <2 towers connected.
 * Per UX-05: Dedicated screen for multi-tower orchestration.
 */
@Composable
fun OrchestrateScreen(
    viewModel: OrchestrateViewModel = hiltViewModel()
) {
    val orderedTowers by viewModel.orderedTowers.collectAsState()
    val orchestrationMode by viewModel.orchestrationMode.collectAsState()
    val offsetDelayMs by viewModel.offsetDelayMs.collectAsState()
    val towerAnimations by viewModel.towerAnimations.collectAsState()
    val animations by viewModel.animations.collectAsState()
    val selectedAnimation by viewModel.selectedAnimation.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    // Per D-11: Show empty state when <2 towers connected
    if (orderedTowers.size < 2) {
        EmptyState()
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Section: Mode Selection
        Text(
            text = "Orchestration Mode",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OrchestrationModeSelector(
            selectedMode = orchestrationMode,
            onModeSelected = viewModel::setMode
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Section: Tower Order
        Text(
            text = "Tower Order",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Drag to reorder towers for offset/cascade modes",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        TowerOrderList(
            towers = orderedTowers,
            onReorder = viewModel::reorderTowers,
            modifier = Modifier.height((orderedTowers.size * 56).dp.coerceAtMost(224.dp))
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Section: Mode-specific settings
        when (orchestrationMode) {
            OrchestrationMode.MIRROR -> {
                // Mirror mode: Just select animation
                AnimationSelector(
                    animations = animations,
                    selectedAnimation = selectedAnimation,
                    onAnimationSelected = viewModel::selectAnimation
                )
            }
            OrchestrationMode.OFFSET -> {
                // Offset mode: Animation + delay slider
                AnimationSelector(
                    animations = animations,
                    selectedAnimation = selectedAnimation,
                    onAnimationSelected = viewModel::selectAnimation
                )
                Spacer(modifier = Modifier.height(16.dp))
                OffsetDelaySlider(
                    delayMs = offsetDelayMs,
                    onDelayChanged = viewModel::setOffsetDelay
                )
            }
            OrchestrationMode.CASCADE -> {
                // Cascade mode: Just select animation (relay is automatic)
                AnimationSelector(
                    animations = animations,
                    selectedAnimation = selectedAnimation,
                    onAnimationSelected = viewModel::selectAnimation
                )
                Text(
                    text = "Each tower starts when the previous completes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            OrchestrationMode.INDEPENDENT -> {
                // Independent mode: Per-tower animation selection
                IndependentTowerConfigList(
                    towers = orderedTowers,
                    animations = animations,
                    towerAnimations = towerAnimations,
                    onAnimationSelected = viewModel::setTowerAnimation
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Playback controls
        if (orchestrationMode == OrchestrationMode.INDEPENDENT) {
            Button(
                onClick = {
                    if (isPlaying) viewModel.stopPlayback() else viewModel.playIndependent()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Stop" else "Play"
                )
                Text(
                    text = if (isPlaying) "Stop All" else "Play Independent",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        } else {
            Button(
                onClick = {
                    if (isPlaying) viewModel.stopPlayback() else viewModel.playOrchestrated()
                },
                enabled = selectedAnimation != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Stop" else "Play"
                )
                Text(
                    text = if (isPlaying) "Stop" else "Play ${orchestrationMode.displayName}",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        // Info text
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = getModeDescription(orchestrationMode),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Empty state shown when <2 towers connected.
 * Per D-11: Shows "Connect more towers" message.
 */
@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.DevicesOther,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Connect more towers",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Orchestration requires at least 2 connected towers",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}

/**
 * Animation selection using FilterChips for non-independent modes.
 */
@Composable
private fun AnimationSelector(
    animations: List<Animation>,
    selectedAnimation: Animation?,
    onAnimationSelected: (Animation?) -> Unit
) {
    Column {
        Text(
            text = "Select Animation",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        if (animations.isEmpty()) {
            Text(
                text = "No saved animations. Create one in the Editor first.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            // Simple list of animation chips (compact, familiar)
            animations.take(5).forEach { animation ->
                FilterChip(
                    selected = selectedAnimation?.id == animation.id,
                    onClick = { onAnimationSelected(animation) },
                    label = { Text(animation.name) },
                    modifier = Modifier.padding(end = 8.dp, bottom = 4.dp)
                )
            }
            if (animations.size > 5) {
                Text(
                    text = "+ ${animations.size - 5} more in Presets tab",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun getModeDescription(mode: OrchestrationMode): String = when (mode) {
    OrchestrationMode.MIRROR -> "All towers show the same animation simultaneously"
    OrchestrationMode.OFFSET -> "Each tower starts with a delay after the previous"
    OrchestrationMode.CASCADE -> "Relay: next tower starts when previous finishes"
    OrchestrationMode.INDEPENDENT -> "Each tower plays its own assigned animation"
}
