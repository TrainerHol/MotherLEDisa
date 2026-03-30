package com.motherledisa.data.ble

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.ktx.suspend
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Connected tower with its command queue.
 */
data class ConnectedTower(
    val address: String,
    val name: String,
    val bleManager: TowerBleManager,
    val commandQueue: TowerCommandQueue
)

/**
 * Manages BLE connections to multiple LED towers.
 *
 * Responsibilities:
 * - Connect/disconnect devices
 * - Maintain list of connected towers
 * - Route commands through per-device command queues
 * - Handle MELK device initialization
 *
 * Reference: RESEARCH.md Pattern 2 - MELK Device Initialization
 */
@Singleton
class TowerConnectionManager @Inject constructor(
    private val context: Context
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _connectedTowers = MutableStateFlow<List<ConnectedTower>>(emptyList())
    /** Flow of currently connected towers */
    val connectedTowers: StateFlow<List<ConnectedTower>> = _connectedTowers.asStateFlow()

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Idle)
    /** Current connection state for UI feedback */
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    /** Connection timeout in milliseconds */
    private val connectionTimeoutMs = 10_000L

    /**
     * Connects to a BLE device.
     * Handles service discovery, MTU negotiation, and MELK initialization.
     *
     * @param address MAC address of the device
     * @param deviceName Name of the device (for MELK detection)
     * @return Result indicating success or failure
     */
    suspend fun connect(address: String, deviceName: String): Result<ConnectedTower> {
        // Check if already connected
        if (_connectedTowers.value.any { it.address == address }) {
            Timber.d("Already connected to $address")
            return Result.success(_connectedTowers.value.first { it.address == address })
        }

        _connectionState.value = ConnectionState.Connecting(address)

        return try {
            Timber.d("Connecting to $deviceName ($address)")

            val bleManager = TowerBleManager(context)

            // Connect with timeout
            withTimeout(connectionTimeoutMs) {
                bleManager.connect(address).suspend()
            }

            // Get the write characteristic
            val characteristic = bleManager.writeCharacteristic
                ?: throw IllegalStateException("Write characteristic not found")

            // Create command queue
            val commandQueue = TowerCommandQueue(bleManager, characteristic)

            // Initialize MELK device if needed
            if (MelkProtocol.isMelkDevice(deviceName)) {
                Timber.d("Device is MELK, running initialization sequence")
                MelkProtocol.initializeMelkDevice { data ->
                    commandQueue.writeRaw(data)
                }
            }

            val connectedTower = ConnectedTower(
                address = address,
                name = deviceName,
                bleManager = bleManager,
                commandQueue = commandQueue
            )

            // Add to connected list
            _connectedTowers.update { it + connectedTower }

            _connectionState.value = ConnectionState.Connected(address)
            Timber.i("Connected to $deviceName ($address)")

            Result.success(connectedTower)
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.Error(
                message = e.message ?: "Connection failed",
                address = address
            )
            Timber.e(e, "Connection failed: $address")
            Result.failure(e)
        }
    }

    /**
     * Disconnects from a tower.
     */
    suspend fun disconnect(tower: ConnectedTower) {
        _connectionState.value = ConnectionState.Disconnecting(tower.address)

        try {
            tower.commandQueue.close()
            tower.bleManager.disconnect().enqueue()

            _connectedTowers.update { it - tower }

            _connectionState.value = ConnectionState.Idle
            Timber.i("Disconnected from ${tower.name}")
        } catch (e: Exception) {
            Timber.e(e, "Disconnect error: ${tower.address}")
            _connectionState.value = ConnectionState.Idle
        }
    }

    /**
     * Disconnects from all towers.
     */
    suspend fun disconnectAll() {
        _connectedTowers.value.forEach { tower ->
            disconnect(tower)
        }
    }

    // ========== Command Routing ==========

    /**
     * Sends a power on/off command to a tower.
     */
    fun setPower(tower: ConnectedTower, on: Boolean) {
        scope.launch {
            val command = if (on) MelkProtocol.powerOn() else MelkProtocol.powerOff()
            tower.commandQueue.enqueue(BleCommand.Power(command))
        }
    }

    /**
     * Sends a color command to a tower.
     */
    fun setColor(tower: ConnectedTower, r: Int, g: Int, b: Int) {
        scope.launch {
            val command = MelkProtocol.setColor(r, g, b)
            tower.commandQueue.enqueue(BleCommand.SetColor(command))
        }
    }

    /**
     * Sends a brightness command to a tower.
     */
    fun setBrightness(tower: ConnectedTower, level: Int) {
        scope.launch {
            val command = MelkProtocol.setBrightness(level)
            tower.commandQueue.enqueue(BleCommand.SetBrightness(command))
        }
    }

    /**
     * Sends an effect command to a tower.
     */
    fun setEffect(tower: ConnectedTower, effectId: Byte, speed: Int) {
        scope.launch {
            val command = MelkProtocol.setEffect(effectId, speed)
            tower.commandQueue.enqueue(BleCommand.SetEffect(command))
        }
    }

    /**
     * Sends commands to all connected towers.
     */
    fun setColorAll(r: Int, g: Int, b: Int) {
        _connectedTowers.value.forEach { tower ->
            setColor(tower, r, g, b)
        }
    }

    fun setBrightnessAll(level: Int) {
        _connectedTowers.value.forEach { tower ->
            setBrightness(tower, level)
        }
    }

    fun setPowerAll(on: Boolean) {
        _connectedTowers.value.forEach { tower ->
            setPower(tower, on)
        }
    }

    fun setEffectAll(effectId: Byte, speed: Int) {
        _connectedTowers.value.forEach { tower ->
            setEffect(tower, effectId, speed)
        }
    }

    // ========== Sound Mode Commands ==========

    /**
     * Sends a generic command to a tower's BLE queue.
     * Used by sound mode use cases for mic control commands.
     *
     * @param address MAC address of the target tower
     * @param command Command bytes to send
     */
    fun sendCommand(address: String, command: ByteArray) {
        val tower = _connectedTowers.value.find { it.address == address }
        if (tower == null) {
            Timber.w("Cannot send command: tower $address not connected")
            return
        }
        scope.launch {
            tower.commandQueue.enqueue(BleCommand.SetColor(command)) // Generic command routing
        }
    }

    /**
     * Enables microphone sound-reactive mode on a tower.
     */
    fun enableMic(tower: ConnectedTower) {
        scope.launch {
            val command = MelkProtocol.enableMic()
            tower.commandQueue.enqueue(BleCommand.EnableMic(command))
        }
    }

    /**
     * Disables microphone sound-reactive mode on a tower.
     */
    fun disableMic(tower: ConnectedTower) {
        scope.launch {
            val command = MelkProtocol.disableMic()
            tower.commandQueue.enqueue(BleCommand.DisableMic(command))
        }
    }

    /**
     * Sets the microphone sound effect on a tower.
     */
    fun setMicEffect(tower: ConnectedTower, effectId: Byte) {
        scope.launch {
            val command = MelkProtocol.setMicEffect(effectId)
            tower.commandQueue.enqueue(BleCommand.SetMicEffect(command))
        }
    }

    /**
     * Sets the microphone sensitivity on a tower.
     */
    fun setMicSensitivity(tower: ConnectedTower, value: Int) {
        scope.launch {
            val command = MelkProtocol.setMicSensitivity(value)
            tower.commandQueue.enqueue(BleCommand.SetMicSensitivity(command))
        }
    }

    /**
     * Enables microphone on all connected towers.
     */
    fun enableMicAll() {
        _connectedTowers.value.forEach { tower ->
            enableMic(tower)
        }
    }

    /**
     * Disables microphone on all connected towers.
     */
    fun disableMicAll() {
        _connectedTowers.value.forEach { tower ->
            disableMic(tower)
        }
    }

    /**
     * Sets microphone effect on all connected towers.
     */
    fun setMicEffectAll(effectId: Byte) {
        _connectedTowers.value.forEach { tower ->
            setMicEffect(tower, effectId)
        }
    }

    /**
     * Sets microphone sensitivity on all connected towers.
     */
    fun setMicSensitivityAll(value: Int) {
        _connectedTowers.value.forEach { tower ->
            setMicSensitivity(tower, value)
        }
    }
}

/**
 * Nordic BLE Manager implementation for a single tower device.
 */
class TowerBleManager(private val appContext: Context) : BleManager(appContext) {

    var writeCharacteristic: BluetoothGattCharacteristic? = null
        private set

    override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
        val service = gatt.getService(UUID.fromString(MelkProtocol.SERVICE_UUID))
        if (service == null) {
            Timber.e("Required service not found: ${MelkProtocol.SERVICE_UUID}")
            return false
        }

        writeCharacteristic = service.getCharacteristic(UUID.fromString(MelkProtocol.CHAR_UUID))
        if (writeCharacteristic == null) {
            Timber.e("Write characteristic not found: ${MelkProtocol.CHAR_UUID}")
            return false
        }

        Timber.d("Required service and characteristic found")
        return true
    }

    override fun initialize() {
        // Request MTU increase for efficiency
        requestMtu(255).enqueue()
        Timber.d("BLE manager initialized, MTU requested")
    }

    override fun onServicesInvalidated() {
        writeCharacteristic = null
        Timber.d("Services invalidated")
    }

    /**
     * Public wrapper around the protected writeCharacteristic method.
     * Needed because TowerCommandQueue calls this from outside the BleManager hierarchy.
     */
    fun writeToCharacteristic(
        characteristic: BluetoothGattCharacteristic,
        data: ByteArray,
        writeType: Int
    ) {
        writeCharacteristic(characteristic, data, writeType).enqueue()
    }

    fun connect(address: String): no.nordicsemi.android.ble.ConnectRequest {
        val bluetoothManager = appContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = bluetoothManager.adapter
            ?: throw IllegalStateException("Bluetooth adapter not available")
        val device = adapter.getRemoteDevice(address)
        return connect(device)
            .retry(3, 100)
            .useAutoConnect(true) // D-13: Persistent auto-reconnect
            .timeout(10_000)
    }
}
