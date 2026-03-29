package com.motherledisa.ui.control.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.godaddy.android.colorpicker.HarmonyColorPicker
import com.godaddy.android.colorpicker.harmony.ColorHarmonyMode
import com.motherledisa.ui.theme.PrimaryAccent

/**
 * Preset colors per UI-SPEC D-11.
 * 8 quick-access swatches.
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
 * Color name lookup for accessibility.
 */
private fun getColorName(color: Color): String = when (color) {
    Color(0xFFFF0000) -> "Red"
    Color(0xFFFF7F00) -> "Orange"
    Color(0xFFFFFF00) -> "Yellow"
    Color(0xFF00FF00) -> "Green"
    Color(0xFF00FFFF) -> "Cyan"
    Color(0xFF0000FF) -> "Blue"
    Color(0xFF8B00FF) -> "Purple"
    Color(0xFFFFFFFF) -> "White"
    else -> "Color"
}

/**
 * Color picker section with HSV wheel and preset swatches.
 *
 * Per D-08: Circular color wheel for hue with saturation/brightness
 * Per D-11: Row of 8 quick-access preset color swatches
 * Per D-21: Color wheel modifies base color used by active effect
 */
@Composable
fun ColorPickerSection(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = "Color",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // D-08: Circular HSV color wheel
        HarmonyColorPicker(
            harmonyMode = ColorHarmonyMode.NONE,
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.CenterHorizontally),
            onColorChanged = { envelope ->
                onColorSelected(envelope.color)  // Continuous updates
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // D-11: Quick-access color swatches
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(presetColors) { color ->
                ColorSwatch(
                    color = color,
                    isSelected = color == selectedColor,
                    onClick = { onColorSelected(color) }
                )
            }
        }
    }
}

/**
 * Individual color swatch button.
 *
 * Per UI-SPEC:
 * - 48dp touch target
 * - 1px gray border when unselected
 * - 3px purple border when selected
 */
@Composable
private fun ColorSwatch(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)  // 48dp touch target per UI-SPEC
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) PrimaryAccent else Color.Gray,
                shape = CircleShape
            )
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = getColorName(color)
            }
    )
}
