package com.motherledisa.ui.preset.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.motherledisa.domain.model.Animation
import com.motherledisa.ui.theme.PrimaryAccent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Grid card for a single preset.
 * Per D-13: Grid layout with animation name and visual thumbnail.
 * Per D-14: Single tap to preview, long-press for options.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PresetCard(
    animation: Animation,
    isPlaying: Boolean,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onTap,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongPress()
                }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPlaying) {
                PrimaryAccent.copy(alpha = 0.2f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isPlaying) 4.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            // Thumbnail preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.5f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1A1A1A)),
                contentAlignment = Alignment.Center
            ) {
                PresetThumbnail(
                    animation = animation,
                    modifier = Modifier.fillMaxSize()
                )

                // Playing indicator
                if (isPlaying) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(PrimaryAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Playing",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Animation name
            Text(
                text = animation.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Metadata
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatDuration(animation.durationMs),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${animation.keyframes.size} keyframes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Simple thumbnail showing keyframe colors.
 * Generates on-demand per RESEARCH.md recommendation.
 */
@Composable
private fun PresetThumbnail(
    animation: Animation,
    modifier: Modifier = Modifier
) {
    // Extract unique colors from keyframes for thumbnail
    val colors = remember(animation.keyframes) {
        animation.keyframes
            .map { Color(it.color) }
            .distinct()
            .take(5)
            .ifEmpty { listOf(Color.Gray) }
    }

    Canvas(modifier = modifier.padding(8.dp)) {
        val segmentWidth = size.width / colors.size
        val cornerRadius = 4.dp.toPx()

        colors.forEachIndexed { index, color ->
            drawRoundRect(
                color = color,
                topLeft = Offset(index * segmentWidth + 2, 0f),
                size = Size(segmentWidth - 4, size.height),
                cornerRadius = CornerRadius(cornerRadius, cornerRadius)
            )
        }
    }
}

/**
 * Format duration as human-readable string.
 */
private fun formatDuration(ms: Long): String {
    val seconds = ms / 1000
    return if (seconds < 60) {
        "${seconds}s"
    } else {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        "${minutes}m ${remainingSeconds}s"
    }
}

/**
 * Format timestamp as relative time.
 */
fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        diff < 604800_000 -> "${diff / 86400_000}d ago"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
    }
}
