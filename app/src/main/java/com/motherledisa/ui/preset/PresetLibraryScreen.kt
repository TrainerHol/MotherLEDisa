package com.motherledisa.ui.preset

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.motherledisa.domain.model.PlaybackState
import com.motherledisa.ui.control.components.DevicePicker
import com.motherledisa.ui.preset.components.PresetCard
import com.motherledisa.ui.preset.components.PresetOptionsMenu

/**
 * Preset Library screen.
 * Per UX-06: Dedicated screen for preset library.
 * Per D-13: Grid layout with animation name and visual thumbnail.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresetLibraryScreen(
    onNavigateToEditor: (animationId: Long?) -> Unit,
    viewModel: PresetViewModel = hiltViewModel()
) {
    val presets by viewModel.presets.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val playingAnimationId by viewModel.playingAnimationId.collectAsState()
    val selectedAnimation by viewModel.selectedAnimation.collectAsState()
    val connectedTowers by viewModel.connectedTowers.collectAsState()
    val selectedTowerAddress by viewModel.selectedTowerAddress.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Presets") },
                actions = {
                    // Stop button when playing
                    if (playbackState == PlaybackState.PLAYING) {
                        IconButton(onClick = { viewModel.stopPlayback() }) {
                            Icon(Icons.Default.Stop, contentDescription = "Stop")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToEditor(null) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Animation")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Device picker (if multiple towers connected)
            if (connectedTowers.size > 1) {
                DevicePicker(
                    towers = connectedTowers,
                    selectedAddress = selectedTowerAddress,
                    onSelectionChanged = { viewModel.selectTower(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (presets.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No animations yet",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap + to create your first animation",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Grid of presets per D-13
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(presets, key = { it.id }) { animation ->
                        PresetCard(
                            animation = animation,
                            isPlaying = playingAnimationId == animation.id,
                            onTap = { viewModel.previewAnimation(animation) },
                            onLongPress = { viewModel.showOptions(animation) }
                        )
                    }
                }
            }
        }
    }

    // Options menu dialog
    selectedAnimation?.let { animation ->
        PresetOptionsMenu(
            animation = animation,
            onApply = { viewModel.applyAnimation(animation) },
            onEdit = { onNavigateToEditor(animation.id) },
            onRename = { newName -> viewModel.renameAnimation(animation, newName) },
            onDuplicate = { viewModel.duplicateAnimation(animation) },
            onDelete = { viewModel.deleteAnimation(animation) },
            onDismiss = { viewModel.dismissOptions() }
        )
    }
}
