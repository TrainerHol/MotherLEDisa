package com.motherledisa.ui.device

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.motherledisa.data.ble.ConnectionState
import com.motherledisa.domain.model.DeviceListItem
import com.motherledisa.ui.device.components.DeviceListItemRow
import com.motherledisa.ui.device.components.RenameDialog
import com.motherledisa.ui.navigation.Screen

/**
 * Device discovery and list screen.
 * Implements continuous scanning (D-01), connection (D-03), and rename (D-16).
 *
 * @param navController Navigation controller for screen transitions
 * @param viewModel Device list ViewModel (injected via Hilt)
 */
@Composable
fun DeviceListScreen(
    navController: NavController,
    viewModel: DeviceViewModel = hiltViewModel()
) {
    val devices by viewModel.deviceList.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    var showRenameDialog by remember { mutableStateOf<DeviceListItem?>(null) }

    // D-01: Continuous scanning while on screen
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.startScanning()
                Lifecycle.Event.ON_PAUSE -> viewModel.stopScanning()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Navigate to control screen when connected
    LaunchedEffect(connectionState) {
        if (connectionState is ConnectionState.Connected) {
            val address = (connectionState as ConnectionState.Connected).address
            navController.navigate(Screen.Control(deviceAddress = address))
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        if (devices.isEmpty()) {
            // D-04: Empty state
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                        .clickable { viewModel.startScanning() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No devices found. Tap to scan again.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(
                items = devices,
                key = { it.address }
            ) { device ->
                DeviceListItemRow(
                    device = device,
                    connectionState = connectionState,
                    onClick = {
                        // D-03: Single tap connects if not already connected
                        if (!device.isConnected) {
                            viewModel.connect(device.address)
                        }
                    },
                    onLongClick = {
                        // D-16: Long-press for rename
                        showRenameDialog = device
                    }
                )
            }
        }
    }

    // Rename dialog
    showRenameDialog?.let { device ->
        RenameDialog(
            currentName = device.displayName,
            onConfirm = { newName ->
                viewModel.renameDevice(device.address, newName)
                showRenameDialog = null
            },
            onDismiss = { showRenameDialog = null }
        )
    }
}
