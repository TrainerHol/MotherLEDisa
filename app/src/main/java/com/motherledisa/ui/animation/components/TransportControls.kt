package com.motherledisa.ui.animation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.motherledisa.domain.model.PlaybackState
import com.motherledisa.ui.theme.PrimaryAccent

/**
 * Transport controls for animation playback.
 * Per D-12: Play, Pause, Stop buttons.
 */
@Composable
fun TransportControls(
    playbackState: PlaybackState,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Play/Pause toggle
        IconButton(
            onClick = {
                when (playbackState) {
                    PlaybackState.PLAYING -> onPause()
                    PlaybackState.PAUSED, PlaybackState.STOPPED -> onPlay()
                }
            },
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = PrimaryAccent,
                contentColor = Color.White
            ),
            modifier = Modifier
                .size(56.dp)
                .semantics {
                    contentDescription = when (playbackState) {
                        PlaybackState.PLAYING -> "Pause"
                        else -> "Play"
                    }
                }
        ) {
            Icon(
                imageVector = if (playbackState == PlaybackState.PLAYING) {
                    Icons.Default.Pause
                } else {
                    Icons.Default.PlayArrow
                },
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
        }

        // Stop button
        IconButton(
            onClick = onStop,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color(0xFF424242),
                contentColor = Color.White
            ),
            enabled = playbackState != PlaybackState.STOPPED,
            modifier = Modifier
                .size(48.dp)
                .semantics { contentDescription = "Stop" }
        ) {
            Icon(
                imageVector = Icons.Default.Stop,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
