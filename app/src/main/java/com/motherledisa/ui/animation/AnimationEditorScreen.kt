package com.motherledisa.ui.animation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.motherledisa.domain.model.PlaybackState
import com.motherledisa.domain.model.TowerState
import com.motherledisa.ui.animation.components.AddKeyframeMenu
import com.motherledisa.ui.animation.components.KeyframeEditor
import com.motherledisa.ui.animation.components.LoopModeSelector
import com.motherledisa.ui.animation.components.TimelineCanvas
import com.motherledisa.ui.animation.components.TransportControls
import com.motherledisa.ui.control.components.DevicePicker
import com.motherledisa.ui.control.components.TowerPreviewCanvas

/**
 * Animation Editor screen.
 * Per UX-03: Dedicated screen for timeline animation editor.
 * Per D-09: Fixed tower preview above timeline.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimationEditorScreen(
    onNavigateBack: () -> Unit,
    viewModel: AnimationEditorViewModel = hiltViewModel()
) {
    val animation by viewModel.animation.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val playheadTimeMs by viewModel.playheadTimeMs.collectAsState()
    val currentFrame by viewModel.currentFrame.collectAsState()
    val selectedKeyframe by viewModel.selectedKeyframe.collectAsState()
    val connectedTowers by viewModel.connectedTowers.collectAsState()
    val selectedTowerAddress by viewModel.selectedTowerAddress.collectAsState()
    val showSaveDialog by viewModel.showSaveDialog.collectAsState()
    val addKeyframeState by viewModel.addKeyframeState.collectAsState()

    // Convert FrameState to TowerState for preview
    val towerState = remember(currentFrame, animation) {
        TowerState(
            isPoweredOn = true,
            currentColor = currentFrame?.segmentColors?.get(0)?.let { Color(it) } ?: Color.White,
            brightness = currentFrame?.segmentBrightness?.get(0) ?: 100,
            segmentColors = currentFrame?.segmentColors?.mapValues { Color(it.value) } ?: emptyMap()
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(animation.name.ifEmpty { "New Animation" }) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showSaveDialog() }) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // D-09: Fixed tower preview above timeline
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1A1A1A)),
                contentAlignment = Alignment.Center
            ) {
                TowerPreviewCanvas(
                    towerState = towerState,
                    modifier = Modifier.height(180.dp)
                )
            }

            // Transport controls and device picker
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TransportControls(
                    playbackState = playbackState,
                    onPlay = {
                        if (playbackState == PlaybackState.PAUSED) {
                            viewModel.resume()
                        } else {
                            viewModel.play()
                        }
                    },
                    onPause = { viewModel.pause() },
                    onStop = { viewModel.stop() }
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Device picker (only show if multiple towers)
                if (connectedTowers.size > 1) {
                    DevicePicker(
                        towers = connectedTowers,
                        selectedAddress = selectedTowerAddress,
                        onSelectionChanged = { viewModel.selectTower(it) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Time display
            Text(
                text = "${playheadTimeMs}ms / ${animation.durationMs}ms",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Timeline canvas
            TimelineCanvas(
                animation = animation,
                playheadTimeMs = playheadTimeMs,
                selectedKeyframe = selectedKeyframe,
                onPlayheadDragged = { viewModel.seekTo(it) },
                onKeyframeDragged = { kf, time -> viewModel.moveKeyframe(kf, time) },
                onKeyframeTapped = { viewModel.selectKeyframe(it) },
                onTrackLongPress = { segment, time -> viewModel.showAddKeyframeMenu(segment, time) },
                onEmptyTap = { viewModel.deselectKeyframe() },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            // Loop mode selector
            LoopModeSelector(
                loopMode = animation.loopMode,
                loopCount = animation.loopCount,
                onLoopModeChanged = { viewModel.setLoopMode(it) },
                onLoopCountChanged = { viewModel.setLoopCount(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }

    // Keyframe editor dialog
    selectedKeyframe?.let { keyframe ->
        KeyframeEditor(
            keyframe = keyframe,
            onSave = { updated -> viewModel.updateKeyframe(keyframe, updated) },
            onDelete = { viewModel.deleteKeyframe(keyframe) },
            onDismiss = { viewModel.deselectKeyframe() }
        )
    }

    // Add keyframe menu
    addKeyframeState?.let { state ->
        AddKeyframeMenu(
            segment = state.segment,
            timeMs = state.timeMs,
            onAddKeyframe = { color -> viewModel.addKeyframe(state.segment, state.timeMs, color) },
            onDismiss = { viewModel.dismissAddKeyframeMenu() }
        )
    }

    // Save dialog
    if (showSaveDialog) {
        SaveAnimationDialog(
            currentName = animation.name,
            onSave = { name -> viewModel.saveAnimation(name) },
            onDismiss = { viewModel.dismissSaveDialog() }
        )
    }
}

/**
 * Dialog for saving animation with a name.
 */
@Composable
private fun SaveAnimationDialog(
    currentName: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(currentName.ifEmpty { "My Animation" }) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save Animation") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Animation Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { onSave(name) },
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
