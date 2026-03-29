package com.motherledisa.ui.sound

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.godaddy.android.colorpicker.ClassicColorPicker
import com.godaddy.android.colorpicker.HsvColor
import com.motherledisa.domain.model.TowerState
import com.motherledisa.ui.control.components.DevicePicker
import com.motherledisa.ui.control.components.TowerPreviewCanvas
import com.motherledisa.ui.sound.components.PalettePickerSection
import com.motherledisa.ui.sound.components.SensitivitySlider
import com.motherledisa.ui.sound.components.SoundEffectSelector
import com.motherledisa.ui.sound.components.SoundModeToggle

/**
 * Sound-reactive configuration screen.
 * Per UX-04: Dedicated screen for sound-reactive configuration.
 * Per D-10: Single scrollable screen following ControlScreen pattern.
 * Per D-02/D-03: Fire-and-forget configuration - tower runs autonomously.
 */
@Composable
fun SoundReactiveScreen(
    viewModel: SoundReactiveViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val connectedTowers by viewModel.connectedTowers.collectAsState()
    val selectedAddress by viewModel.selectedTowerAddress.collectAsState()
    val savedAnimations by viewModel.savedAnimations.collectAsState()

    // Create preview state based on current palette
    val previewState = TowerState(
        isPoweredOn = uiState.isSoundModeEnabled,
        currentColor = uiState.palette.primaryColor,
        segmentColors = (0 until 5).associateWith { uiState.palette.primaryColor },
        brightness = 100
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Device picker (only if multiple towers connected)
        if (connectedTowers.size > 1) {
            DevicePicker(
                towers = connectedTowers,
                selectedAddress = selectedAddress,
                onSelectionChanged = viewModel::selectTower
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Tower preview
        TowerPreviewCanvas(
            towerState = previewState,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Sound mode toggle (main enable/disable)
        SoundModeToggle(
            enabled = uiState.isSoundModeEnabled,
            onToggle = viewModel::toggleSoundMode
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Effect selector
        SoundEffectSelector(
            selectedEffect = uiState.selectedEffect,
            onEffectSelected = viewModel::selectEffect,
            enabled = uiState.isSoundModeEnabled
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Sensitivity slider
        SensitivitySlider(
            sensitivity = uiState.sensitivity,
            onSensitivityChanged = viewModel::setSensitivity,
            enabled = uiState.isSoundModeEnabled
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Palette picker
        PalettePickerSection(
            palette = uiState.palette,
            onPaletteChanged = viewModel::setPalette,
            savedAnimations = savedAnimations,
            onAddColorClick = { viewModel.setColorPickerVisible(true) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Info text about autonomous operation
        Text(
            text = "Tower will respond to sound autonomously. You can disconnect the app after enabling.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )

        // Bottom padding for navigation bar
        Spacer(modifier = Modifier.height(80.dp))

        // Color picker dialog (when adding new color to palette)
        if (uiState.isColorPickerVisible) {
            ColorPickerDialog(
                onColorSelected = { color ->
                    viewModel.addColorToPalette(color)
                    viewModel.setColorPickerVisible(false)
                },
                onDismiss = { viewModel.setColorPickerVisible(false) }
            )
        }
    }
}

/**
 * Color picker dialog for adding colors to palette.
 */
@Composable
private fun ColorPickerDialog(
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedColor by remember { mutableStateOf(HsvColor.from(Color.Red)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Color") },
        text = {
            ClassicColorPicker(
                color = selectedColor,
                onColorChanged = { hsvColor ->
                    selectedColor = hsvColor
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onColorSelected(selectedColor.toColor()) }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
