package com.motherledisa.data.ble

import android.bluetooth.BluetoothGattCharacteristic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.WriteRequest
import timber.log.Timber

/**
 * Command queue for serializing BLE operations to a single tower.
 *
 * CRITICAL: Android BLE stack cannot handle concurrent operations.
 * All writes must be serialized through this queue to prevent:
 * - GATT_ERROR 133 (concurrent operation errors)
 * - Silent command failures
 * - Connection drops
 *
 * Pattern: Channel-based queue processing one command at a time with 20ms delay.
 * Reference: RESEARCH.md Pattern 1 - BLE Command Queue
 */
class TowerCommandQueue(
    private val bleManager: BleManager,
    private val characteristic: BluetoothGattCharacteristic
) {
    private val commandChannel = Channel<BleCommand>(Channel.UNLIMITED)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /** Time between commands in milliseconds (prevents BLE stack overflow) */
    private val commandDelayMs = 20L

    /** Timeout for individual commands in milliseconds */
    private val commandTimeoutMs = 5000L

    init {
        scope.launch {
            processCommands()
        }
    }

    /**
     * Enqueues a command for execution.
     * Commands are processed in FIFO order, one at a time.
     */
    suspend fun enqueue(command: BleCommand) {
        commandChannel.send(command)
    }

    /**
     * Main command processing loop.
     * Processes commands sequentially with delay between each.
     */
    private suspend fun processCommands() {
        for (command in commandChannel) {
            try {
                executeCommand(command)
                delay(commandDelayMs)
            } catch (e: Exception) {
                Timber.e(e, "BLE command failed: ${command.javaClass.simpleName}")
                // Continue processing next command - don't break the loop
            }
        }
    }

    /**
     * Executes a single BLE write command.
     * Uses WriteType.NO_RESPONSE for speed (animation/color commands).
     */
    private suspend fun executeCommand(command: BleCommand) {
        try {
            bleManager.writeCharacteristic(
                characteristic,
                command.data,
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            ).enqueue()

            Timber.v("Executed BLE command: ${command.javaClass.simpleName} (${command.data.size} bytes)")
        } catch (e: Exception) {
            Timber.e(e, "Failed to write characteristic: ${command.javaClass.simpleName}")
            throw e
        }
    }

    /**
     * Direct write function for initialization sequence (bypasses command queue).
     * Used by MelkProtocol.initializeMelkDevice().
     */
    suspend fun writeRaw(data: ByteArray) {
        try {
            bleManager.writeCharacteristic(
                characteristic,
                data,
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT // WITH_RESPONSE for init
            ).enqueue()

            Timber.d("Raw write: ${data.size} bytes")
        } catch (e: Exception) {
            Timber.e(e, "Raw write failed")
            throw e
        }
    }

    /**
     * Cancels the command queue processing.
     * Call when disconnecting from device.
     */
    fun close() {
        scope.cancel()
        commandChannel.close()
        Timber.d("Command queue closed")
    }
}
