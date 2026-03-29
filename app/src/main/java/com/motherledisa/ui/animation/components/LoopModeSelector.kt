package com.motherledisa.ui.animation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.motherledisa.domain.model.LoopMode

/**
 * Loop mode selector per D-11.
 * Options: once, 2x, 3x, infinite, ping-pong
 */
@Composable
fun LoopModeSelector(
    loopMode: LoopMode,
    loopCount: Int,
    onLoopModeChanged: (LoopMode) -> Unit,
    onLoopCountChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = loopMode == LoopMode.ONCE,
                onClick = { onLoopModeChanged(LoopMode.ONCE) },
                label = { Text("Once") }
            )
            FilterChip(
                selected = loopMode == LoopMode.COUNT,
                onClick = { onLoopModeChanged(LoopMode.COUNT) },
                label = { Text("${loopCount}x") }
            )
            FilterChip(
                selected = loopMode == LoopMode.INFINITE,
                onClick = { onLoopModeChanged(LoopMode.INFINITE) },
                label = { Text("Loop") }
            )
            FilterChip(
                selected = loopMode == LoopMode.PING_PONG,
                onClick = { onLoopModeChanged(LoopMode.PING_PONG) },
                label = { Text("Ping-Pong") }
            )
        }

        // Show count slider when COUNT mode selected
        if (loopMode == LoopMode.COUNT) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Repeat:")
                Spacer(modifier = Modifier.width(8.dp))
                Slider(
                    value = loopCount.toFloat(),
                    onValueChange = { onLoopCountChanged(it.toInt()) },
                    valueRange = 2f..10f,
                    steps = 7,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("$loopCount")
            }
        }
    }
}
