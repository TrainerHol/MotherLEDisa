package com.motherledisa.ui.control.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.motherledisa.data.ble.ConnectedTower

/**
 * Device picker dropdown for selecting which tower to control.
 *
 * Per D-12: Device picker chip/dropdown at top when multiple devices connected.
 *          "All" option applies controls to all devices.
 * Per D-15: Switching devices is instant. Both devices remain connected.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicePicker(
    towers: List<ConnectedTower>,
    selectedAddress: String?,
    onSelectionChanged: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    // Only show when multiple towers connected
    if (towers.size <= 1) return

    var expanded by remember { mutableStateOf(false) }

    val selectedText = when (selectedAddress) {
        null -> "All devices"
        else -> towers.find { it.address == selectedAddress }
            ?.name
            ?: "Unknown"
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedText,
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            // "All devices" option (D-12)
            DropdownMenuItem(
                text = { Text("All devices") },
                onClick = {
                    onSelectionChanged(null)
                    expanded = false
                }
            )

            // Individual tower options
            towers.forEach { tower ->
                DropdownMenuItem(
                    text = { Text(tower.name) },
                    onClick = {
                        onSelectionChanged(tower.address)
                        expanded = false
                    }
                )
            }
        }
    }
}
