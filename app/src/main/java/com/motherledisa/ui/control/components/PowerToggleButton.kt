package com.motherledisa.ui.control.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.motherledisa.ui.theme.OnSurfaceSecondary
import com.motherledisa.ui.theme.SecondaryAccent

/**
 * Power toggle button for LED tower.
 *
 * Per UI-SPEC D-09:
 * - 64dp diameter
 * - Yellow (#FFEB3B / SecondaryAccent) when ON
 * - Gray (#666666) when OFF
 */
@Composable
fun PowerToggleButton(
    isOn: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isOn) SecondaryAccent else Color(0xFF666666),
        animationSpec = tween(200),
        label = "power_bg"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(backgroundColor)
                .clickable(onClick = onClick)
                .semantics { contentDescription = "Power ${if (isOn) "on" else "off"}" },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PowerSettingsNew,
                contentDescription = null,
                tint = if (isOn) Color.Black else Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        // Label below button
        Text(
            text = if (isOn) "ON" else "OFF",
            style = MaterialTheme.typography.labelMedium,
            color = if (isOn) SecondaryAccent else OnSurfaceSecondary,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
