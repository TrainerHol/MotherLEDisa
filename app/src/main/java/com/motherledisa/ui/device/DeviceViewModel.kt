package com.motherledisa.ui.device

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.motherledisa.data.ble.ConnectionState
import com.motherledisa.data.ble.DeviceScanner
import com.motherledisa.data.ble.TowerConnectionManager
import com.motherledisa.data.local.TowerConfigDao
import com.motherledisa.data.local.TowerConfigEntity
import com.motherledisa.domain.model.DeviceListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for the device discovery and list screen.
 *
 * Responsibilities:
 * - Merge scan results with known devices (D-05)
 * - Manage scanning lifecycle (D-01)
 * - Handle connection requests (D-03)
 * - Support device renaming (D-16)
 */
@HiltViewModel
class DeviceViewModel @Inject constructor(
    private val scanner: DeviceScanner,
    private val connectionManager: TowerConnectionManager,
    private val towerConfigDao: TowerConfigDao
) : ViewModel() {

    /**
     * Merged device list combining:
     * - Currently scanned devices (Available)
     * - Known devices not currently visible (Known with "Last connected" badge)
     *
     * Sorted by lastConnected timestamp for most recently used first.
     */
    val deviceList: StateFlow<List<DeviceListItem>> = combine(
        scanner.discoveredDevices,
        towerConfigDao.getAllKnownDevices(),
        connectionManager.connectedTowers
    ) { scanned, known, connected ->
        val connectedAddresses = connected.map { it.address }.toSet()
        val scannedAddresses = scanned.map { it.address }.toSet()
        val knownByAddress = known.associateBy { it.address }

        // Build Available items from scanned devices
        val availableItems = scanned.map { device ->
            val knownConfig = knownByAddress[device.address]
            DeviceListItem.Available(
                address = device.address,
                displayName = knownConfig?.customName ?: device.name,
                rssi = device.rssi,
                isConnected = device.address in connectedAddresses,
                lastConnected = knownConfig?.lastConnected,
                isKnown = knownConfig != null
            )
        }

        // Build Known items for devices not currently visible
        val knownItems = known
            .filterNot { it.address in scannedAddresses }
            .map { config ->
                DeviceListItem.Known(
                    address = config.address,
                    displayName = config.customName ?: config.deviceName,
                    isConnected = config.address in connectedAddresses,
                    lastConnected = config.lastConnected
                )
            }

        // Merge and sort by lastConnected (most recent first)
        (availableItems + knownItems).sortedByDescending { it.lastConnected ?: 0L }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyList()
    )

    /** Current connection state for UI feedback */
    val connectionState: StateFlow<ConnectionState> = connectionManager.connectionState

    /** Whether scanning is currently active */
    val isScanning: StateFlow<Boolean> = scanner.isScanning

    /**
     * Starts continuous BLE scanning.
     * Called when device list screen becomes visible (D-01).
     */
    fun startScanning() {
        Timber.d("Starting device scan")
        scanner.startContinuousScan()
    }

    /**
     * Stops BLE scanning.
     * Called when device list screen becomes hidden.
     */
    fun stopScanning() {
        Timber.d("Stopping device scan")
        scanner.stopScan()
    }

    /**
     * Connects to a device by address.
     * Creates/updates known device entry on successful connection (D-05).
     *
     * @param address MAC address of device to connect
     */
    fun connect(address: String) {
        viewModelScope.launch {
            // Get device name from scanned or known devices
            val deviceName = deviceList.value
                .find { it.address == address }
                ?.displayName ?: address

            val result = connectionManager.connect(address, deviceName)

            if (result.isSuccess) {
                // Update or create known device entry
                val now = System.currentTimeMillis()
                val existing = towerConfigDao.getByAddress(address)
                if (existing != null) {
                    towerConfigDao.updateLastConnected(address, now)
                } else {
                    towerConfigDao.upsert(
                        TowerConfigEntity(
                            address = address,
                            deviceName = deviceName,
                            lastConnected = now
                        )
                    )
                }
                Timber.i("Connected to $deviceName")
            } else {
                Timber.e(result.exceptionOrNull(), "Failed to connect to $deviceName")
            }
        }
    }

    /**
     * Disconnects from a connected device.
     */
    fun disconnect(address: String) {
        viewModelScope.launch {
            connectionManager.connectedTowers.value
                .find { it.address == address }
                ?.let { tower ->
                    connectionManager.disconnect(tower)
                    Timber.i("Disconnected from ${tower.name}")
                }
        }
    }

    /**
     * Updates the custom display name for a device (D-16).
     *
     * @param address MAC address of device to rename
     * @param newName New custom display name
     */
    fun renameDevice(address: String, newName: String) {
        viewModelScope.launch {
            // Ensure device exists in database
            val existing = towerConfigDao.getByAddress(address)
            if (existing != null) {
                towerConfigDao.updateCustomName(address, newName)
                Timber.i("Renamed device $address to $newName")
            } else {
                // Device not yet in database, get name from scan
                val deviceName = deviceList.value
                    .find { it.address == address }
                    ?.displayName ?: address
                towerConfigDao.upsert(
                    TowerConfigEntity(
                        address = address,
                        deviceName = deviceName,
                        customName = newName
                    )
                )
                Timber.i("Created device entry for $address with name $newName")
            }
        }
    }
}
