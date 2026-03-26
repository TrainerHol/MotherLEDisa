package com.motherledisa.ui.device.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/**
 * Permission explanation dialog for BLE/Location permissions.
 * Shown when permissions are denied (D-17).
 *
 * @param onGrantClick Called when user wants to grant permissions
 * @param onDismiss Called when dialog is dismissed
 */
@Composable
fun PermissionDialog(
    onGrantClick: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Bluetooth Permission Required") },
        text = {
            Text(
                "Bluetooth and Location permissions are required to discover nearby LED towers. Tap Grant to continue."
            )
        },
        confirmButton = {
            TextButton(onClick = onGrantClick) {
                Text("Grant")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
