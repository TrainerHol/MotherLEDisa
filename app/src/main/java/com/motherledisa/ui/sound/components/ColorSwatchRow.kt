package com.motherledisa.ui.sound.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Row of color swatches for palette display.
 * Shows primary color indicator (star icon) on selected primary.
 *
 * @param colors List of colors in palette
 * @param primaryIndex Index of primary color (shown with star)
 * @param onColorClick Called when a swatch is clicked (to set as primary)
 * @param onColorLongClick Optional long-click handler (e.g., to remove color)
 */
@Composable
fun ColorSwatchRow(
    colors: List<Color>,
    primaryIndex: Int,
    onColorClick: (Int) -> Unit,
    onColorLongClick: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(colors) { index, color ->
            ColorSwatch(
                color = color,
                isPrimary = index == primaryIndex,
                onClick = { onColorClick(index) },
                onLongClick = onColorLongClick?.let { { it(index) } }
            )
        }
    }
}

@Composable
private fun ColorSwatch(
    color: Color,
    isPrimary: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (isPrimary) 3.dp else 1.dp,
                color = if (isPrimary) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.5f),
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isPrimary) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Primary color",
                tint = if (color.luminance() > 0.5f) Color.Black else Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/** Calculate luminance for contrast detection */
private fun Color.luminance(): Float {
    return (0.299f * red + 0.587f * green + 0.114f * blue)
}
