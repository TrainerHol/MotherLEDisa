package com.motherledisa.ui.animation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.godaddy.android.colorpicker.HarmonyColorPicker
import com.godaddy.android.colorpicker.harmony.ColorHarmonyMode
import com.motherledisa.domain.model.InterpolationMode
import com.motherledisa.domain.model.Keyframe
import com.motherledisa.ui.theme.PrimaryAccent

/**
 * Preset colors for quick selection.
 */
private val presetColors = listOf(
    Color(0xFFFF0000),  // Red
    Color(0xFFFF7F00),  // Orange
    Color(0xFFFFFF00),  // Yellow
    Color(0xFF00FF00),  // Green
    Color(0xFF00FFFF),  // Cyan
    Color(0xFF0000FF),  // Blue
    Color(0xFF8B00FF),  // Purple
    Color(0xFFFFFFFF)   // White
)

/**
 * Dialog for editing keyframe properties.
 * Per D-06: Each keyframe controls Color, Segment, Brightness, optional Effect.
 * Per D-07: User chooses interpolation mode (smooth vs step).
 */
@Composable
fun KeyframeEditor(
    keyframe: Keyframe,
    onSave: (Keyframe) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    var color by remember { mutableStateOf(Color(keyframe.color)) }
    var brightness by remember { mutableIntStateOf(keyframe.brightness) }
    var interpolation by remember { mutableStateOf(keyframe.interpolation) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Edit Keyframe",
                    style = MaterialTheme.typography.titleLarge
                )

                // Segment info (read-only)
                Text(
                    text = "Segment ${keyframe.segment + 1} at ${keyframe.timeMs}ms",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Color section
                Text(
                    text = "Color",
                    style = MaterialTheme.typography.titleMedium
                )

                // Color picker
                HarmonyColorPicker(
                    harmonyMode = ColorHarmonyMode.NONE,
                    modifier = Modifier
                        .size(200.dp)
                        .align(Alignment.CenterHorizontally),
                    color = color,
                    onColorChanged = { envelope ->
                        color = envelope.color
                    }
                )

                // Preset colors
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(presetColors) { presetColor ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(presetColor)
                                .border(
                                    width = if (presetColor == color) 3.dp else 1.dp,
                                    color = if (presetColor == color) PrimaryAccent else Color.Gray,
                                    shape = CircleShape
                                )
                                .clickable { color = presetColor }
                        )
                    }
                }

                // Brightness
                Text(
                    text = "Brightness: $brightness%",
                    style = MaterialTheme.typography.titleMedium
                )

                Slider(
                    value = brightness.toFloat(),
                    onValueChange = { brightness = it.toInt() },
                    valueRange = 0f..100f,
                    modifier = Modifier.fillMaxWidth()
                )

                // Interpolation mode per D-07
                Text(
                    text = "Interpolation",
                    style = MaterialTheme.typography.titleMedium
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = interpolation == InterpolationMode.SMOOTH,
                        onClick = { interpolation = InterpolationMode.SMOOTH },
                        label = { Text("Smooth (HSV)") }
                    )
                    FilterChip(
                        selected = interpolation == InterpolationMode.STEP,
                        onClick = { interpolation = InterpolationMode.STEP },
                        label = { Text("Step/Hold") }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Delete")
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val updatedKeyframe = keyframe.copy(
                                color = color.toArgb(),
                                brightness = brightness,
                                interpolation = interpolation
                            )
                            onSave(updatedKeyframe)
                        }
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

/**
 * Context menu for adding keyframe.
 * Per D-03: Long-press on track shows context menu with "Add keyframe" option.
 */
@Composable
fun AddKeyframeMenu(
    segment: Int,
    timeMs: Long,
    onAddKeyframe: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedColor by remember { mutableStateOf(Color.White) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Add Keyframe",
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = "Segment ${segment + 1} at ${timeMs}ms",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Quick color selection
                Text(
                    text = "Select Color",
                    style = MaterialTheme.typography.titleMedium
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(presetColors) { presetColor ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(presetColor)
                                .border(
                                    width = if (presetColor == selectedColor) 3.dp else 1.dp,
                                    color = if (presetColor == selectedColor) PrimaryAccent else Color.Gray,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = presetColor }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.size(8.dp))

                    Button(onClick = { onAddKeyframe(selectedColor) }) {
                        Text("Add")
                    }
                }
            }
        }
    }
}
