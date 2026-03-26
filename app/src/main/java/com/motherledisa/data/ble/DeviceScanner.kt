package com.motherledisa.data.ble

import android.os.ParcelUuid
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanCallback
import no.nordicsemi.android.support.v18.scanner.ScanFilter
import no.nordicsemi.android.support.v18.scanner.ScanResult
import no.nordicsemi.android.support.v18.scanner.ScanSettings
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Discovered BLE device information.
 */
data class DiscoveredDevice(
    /** MAC address */
    val address: String,
    /** Device name from advertisement */
    val name: String,
    /** Signal strength in dBm */
    val rssi: Int,
    /** Timestamp of last advertisement received */
    val lastSeen: Long
)

/**
 * BLE device scanner for discovering ELK-BLEDOM compatible devices.
 *
 * Uses Nordic Scanner Compat for compatibility across Android versions.
 * Implements continuous scanning with device name filters (D-01).
 *
 * CRITICAL: ScanFilter prevents Android 7+ 30-second scan timeout.
 * Reference: RESEARCH.md Pattern 4 - Continuous Scanning with Filters
 */
@Singleton
class DeviceScanner @Inject constructor() {

    private val scanner: BluetoothLeScannerCompat = BluetoothLeScannerCompat.getScanner()

    private val _discoveredDevices = MutableStateFlow<List<DiscoveredDevice>>(emptyList())
    /** Flow of currently discovered devices */
    val discoveredDevices: StateFlow<List<DiscoveredDevice>> = _discoveredDevices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    /** Whether scanning is currently active */
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            handleScanResult(result)
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            results.forEach { handleScanResult(it) }
        }

        override fun onScanFailed(errorCode: Int) {
            Timber.e("BLE scan failed: errorCode=$errorCode")
            _isScanning.value = false
        }
    }

    private fun handleScanResult(result: ScanResult) {
        val device = result.device
        val name = result.scanRecord?.deviceName ?: device.name ?: return

        // Filter for compatible devices
        if (!MelkProtocol.isCompatibleDevice(name)) {
            return
        }

        val discovered = DiscoveredDevice(
            address = device.address,
            name = name,
            rssi = result.rssi,
            lastSeen = System.currentTimeMillis()
        )

        // Update or add device to list
        _discoveredDevices.update { devices ->
            val updated = devices.toMutableList()
            val existingIndex = updated.indexOfFirst { it.address == discovered.address }
            if (existingIndex >= 0) {
                updated[existingIndex] = discovered
            } else {
                updated.add(discovered)
            }
            updated.sortedByDescending { it.lastSeen }
        }

        Timber.d("Discovered device: $name (${device.address}), RSSI: ${result.rssi}")
    }

    /**
     * Starts continuous BLE scanning for ELK-BLEDOM devices.
     * D-01: Continuous scanning while on device list screen.
     */
    fun startContinuousScan() {
        if (_isScanning.value) {
            Timber.d("Scan already in progress")
            return
        }

        // Clear stale devices
        _discoveredDevices.value = emptyList()

        // Build scan filters for device name prefixes
        // CRITICAL: Filters prevent 30-second timeout on Android 7+
        val filters = listOf(
            // Filter by service UUID (all ELK-BLEDOM devices advertise this)
            ScanFilter.Builder()
                .setServiceUuid(ParcelUuid.fromString(MelkProtocol.SERVICE_UUID))
                .build()
        )

        // Configure scan settings for continuous low-latency scanning
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY) // Continuous, fastest updates
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
            .setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT)
            .setReportDelay(0) // Immediate callback
            .build()

        try {
            scanner.startScan(filters, settings, scanCallback)
            _isScanning.value = true
            Timber.d("Continuous scan started with service UUID filter")
        } catch (e: Exception) {
            Timber.e(e, "Failed to start scan")
            _isScanning.value = false
        }
    }

    /**
     * Stops BLE scanning.
     */
    fun stopScan() {
        if (!_isScanning.value) {
            return
        }

        try {
            scanner.stopScan(scanCallback)
            _isScanning.value = false
            Timber.d("Scan stopped")
        } catch (e: Exception) {
            Timber.e(e, "Failed to stop scan")
            _isScanning.value = false
        }
    }

    /**
     * Removes devices not seen within the timeout period.
     * Call periodically to clean up stale entries.
     */
    fun pruneStaleDevices(maxAgeMs: Long = 10_000) {
        val cutoff = System.currentTimeMillis() - maxAgeMs
        _discoveredDevices.update { devices ->
            devices.filter { it.lastSeen >= cutoff }
        }
    }
}
