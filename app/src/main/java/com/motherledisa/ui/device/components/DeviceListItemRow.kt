package com.motherledisa.ui.device.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Badge
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.motherledisa.data.ble.ConnectionState
import com.motherledisa.domain.model.DeviceListItem
import com.motherledisa.ui.theme.OnSurfaceSecondary
import com.motherledisa.ui.theme.PrimaryAccent

/**
 * Device list item row showing device info, signal strength, and status.
 * Implements D-02 (signal bars), D-03 (tap to connect), D-05 (badges), D-16 (long-press rename).
 *
 * @param device The device item to display
 * @param connectionState Current connection state for loading indicator
 * @param onClick Called on single tap (D-03: connect)
 * @param onLongClick Called on long press (D-16: rename)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DeviceListItemRow(
    device: DeviceListItem,
    connectionState: ConnectionState,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isConnecting = connectionState is ConnectionState.Connecting &&
            connectionState.address == device.address

    ListItem(
        headlineContent = {
            Text(
                text = device.displayName,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        supportingContent = {
            // D-05: "Last connected" badge for known devices not currently connected
            val showLastConnected = when {
                device.isConnected -> false
                device is DeviceListItem.Known -> true
                device is DeviceListItem.Available && device.isKnown -> true
                else -> false
            }

            if (showLastConnected && device.lastConnected != null) {
                Text(
                    text = "Last connected ${formatRelativeTime(device.lastConnected)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = OnSurfaceSecondary
                )
            }
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // D-02: Signal strength bars (only for visible devices)
                if (device.rssi != null) {
                    SignalStrengthBars(rssi = device.rssi)
                    Spacer(modifier = Modifier.width(8.dp))
                }

                when {
                    isConnecting -> {
                        // Loading indicator during connection attempt
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    device.isConnected -> {
                        // D-02: Connected badge
                        Badge(
                            containerColor = PrimaryAccent
                        ) {
                            Text(
                                text = "Connected",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        },
        modifier = modifier.combinedClickable(
            onClick = onClick,     // D-03: Single tap connects
            onLongClick = onLongClick  // D-16: Long-press for rename
        )
    )
}

/**
 * Formats a timestamp as relative time (e.g., "2 hours ago").
 */
private fun formatRelativeTime(timestamp: Long?): String {
    if (timestamp == null) return "never"

    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60_000 -> "just now"
        diff < 3_600_000 -> "${diff / 60_000} min ago"
        diff < 86_400_000 -> "${diff / 3_600_000} hours ago"
        else -> "${diff / 86_400_000} days ago"
    }
}
