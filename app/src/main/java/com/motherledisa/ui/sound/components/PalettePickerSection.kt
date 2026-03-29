package com.motherledisa.ui.sound.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.motherledisa.domain.model.Animation
import com.motherledisa.domain.model.SoundPalette
import com.motherledisa.domain.model.extractPalette

/**
 * Section for selecting/building a color palette for sound-reactive mode.
 * Per D-05: Two ways to define palette:
 * 1. Pick from saved animation preset (extract colors)
 * 2. Build custom palette (2-5 colors)
 *
 * @param palette Current palette
 * @param onPaletteChanged Called when palette changes
 * @param savedAnimations List of saved animations for preset selection
 * @param onAddColorClick Called when user wants to add a color (opens color picker)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PalettePickerSection(
    palette: SoundPalette,
    onPaletteChanged: (SoundPalette) -> Unit,
    savedAnimations: List<Animation>,
    onAddColorClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var presetDropdownExpanded by remember { mutableStateOf(false) }
    var selectedPresetName by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Section header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Color Palette",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Text(
                    text = "Tap to set primary",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Current palette display
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ColorSwatchRow(
                    colors = palette.colors,
                    primaryIndex = palette.primaryIndex,
                    onColorClick = { index ->
                        onPaletteChanged(palette.copy(primaryIndex = index))
                    },
                    onColorLongClick = { index ->
                        // Remove color if more than 1 color in palette
                        if (palette.colors.size > 1) {
                            val newColors = palette.colors.toMutableList().apply { removeAt(index) }
                            val newPrimaryIndex = if (index <= palette.primaryIndex && palette.primaryIndex > 0) {
                                palette.primaryIndex - 1
                            } else {
                                palette.primaryIndex.coerceIn(0, newColors.size - 1)
                            }
                            onPaletteChanged(SoundPalette(newColors, newPrimaryIndex))
                        }
                    },
                    modifier = Modifier.weight(1f)
                )

                // Add color button (only if < 5 colors)
                if (palette.colors.size < 5) {
                    IconButton(onClick = onAddColorClick) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add color",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Preset dropdown (if animations available)
            if (savedAnimations.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                ExposedDropdownMenuBox(
                    expanded = presetDropdownExpanded,
                    onExpandedChange = { presetDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedPresetName ?: "Extract from preset...",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Use preset colors") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = presetDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )

                    ExposedDropdownMenu(
                        expanded = presetDropdownExpanded,
                        onDismissRequest = { presetDropdownExpanded = false }
                    ) {
                        savedAnimations.forEach { animation ->
                            DropdownMenuItem(
                                text = { Text(animation.name) },
                                onClick = {
                                    selectedPresetName = animation.name
                                    val extractedPalette = animation.extractPalette()
                                    onPaletteChanged(extractedPalette)
                                    presetDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
