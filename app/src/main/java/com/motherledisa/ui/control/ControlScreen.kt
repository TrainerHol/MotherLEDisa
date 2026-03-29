package com.motherledisa.ui.control

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.motherledisa.ui.control.components.BrightnessSlider
import com.motherledisa.ui.control.components.ColorPickerSection
import com.motherledisa.ui.control.components.DevicePicker
import com.motherledisa.ui.control.components.EffectsSection
import com.motherledisa.ui.control.components.PowerToggleButton
import com.motherledisa.ui.control.components.SpeedSlider
import com.motherledisa.ui.control.components.TowerPreviewCanvas

/**
 * Main control screen for tower LED control.
 *
 * Per D-06: Single scrollable screen for all controls:
 * - Preview at top
 * - Power toggle
 * - Color picker
 * - Brightness slider
 * - Effects section
 *
 * @param deviceAddress Specific device to control, or null for "All devices" mode
 */
@Composable
fun ControlScreen(
    deviceAddress: String? = null,
    viewModel: ControlViewModel = hiltViewModel()
) {
    val towerState by viewModel.towerState.collectAsState()
    val connectedTowers by viewModel.connectedTowers.collectAsState()
    val selectedAddress by viewModel.selectedTowerAddress.collectAsState()

    // If no towers connected, show empty state
    if (connectedTowers.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No device connected.\nGo to Devices tab to connect.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    // D-06: Single scrollable screen
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // D-12: Device picker (only shown when multiple connected)
        DevicePicker(
            towers = connectedTowers,
            selectedAddress = selectedAddress,
            onSelectionChanged = { viewModel.selectTower(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // D-07: Tower preview at top
        TowerPreviewCanvas(
            towerState = towerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // D-09: Power toggle
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PowerToggleButton(
                isOn = towerState.isPoweredOn,
                onClick = { viewModel.togglePower() }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // D-08, D-11: Color picker with swatches
        ColorPickerSection(
            selectedColor = towerState.currentColor,
            onColorSelected = { viewModel.setColor(it) }
        )

        // D-10: Brightness slider
        BrightnessSlider(
            brightness = towerState.brightness,
            onBrightnessChanged = { viewModel.setBrightness(it) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // D-18, D-19, D-22: Effects section
        EffectsSection(
            effectsByCategory = viewModel.effectsByCategory,
            activeEffect = towerState.activeEffect,
            onEffectSelected = { viewModel.setEffect(it) },
            onClearEffect = { viewModel.clearEffect() }
        )

        // D-20: Speed slider (only when effect active)
        AnimatedVisibility(visible = towerState.activeEffect != null) {
            SpeedSlider(
                speed = towerState.effectSpeed,
                onSpeedChanged = { viewModel.setEffectSpeed(it) },
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        // Bottom padding for navigation bar
        Spacer(modifier = Modifier.height(80.dp))
    }
}
