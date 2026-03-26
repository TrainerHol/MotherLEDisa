package com.motherledisa.data.ble

import kotlinx.coroutines.delay
import no.nordicsemi.android.ble.WriteRequest
import timber.log.Timber
import java.util.UUID

/**
 * ELK-BLEDOM and MELK protocol implementation.
 * All commands are 9 bytes: [0x7E, CMD_BYTE, ...payload..., 0xEF]
 *
 * MELK devices (MELK-OT21) require an initialization sequence after connection
 * before accepting standard ELK-BLEDOM commands.
 */
object MelkProtocol {
    /** Primary BLE service UUID for ELK-BLEDOM devices */
    const val SERVICE_UUID = "0000fff0-0000-1000-8000-00805f9b34fb"

    /** Write characteristic UUID for commands */
    const val CHAR_UUID = "0000fff3-0000-1000-8000-00805f9b34fb"

    /** Service UUID as typed object */
    val serviceUuid: UUID = UUID.fromString(SERVICE_UUID)

    /** Characteristic UUID as typed object */
    val charUuid: UUID = UUID.fromString(CHAR_UUID)

    /** Command frame start byte */
    private const val START_BYTE: Byte = 0x7E

    /** Command frame end byte */
    private const val END_BYTE: Byte = 0xEF.toByte()

    /** Compatible device name prefixes */
    private val COMPATIBLE_PREFIXES = listOf("ELK-", "MELK-", "LEDBLE", "ELK-BULB", "ELK-LAMPL")

    /** MELK-specific device prefix (requires initialization) */
    private const val MELK_PREFIX = "MELK-"

    /**
     * Checks if a device name indicates an ELK-BLEDOM compatible device.
     * Used for filtering BLE scan results.
     */
    fun isCompatibleDevice(name: String?): Boolean {
        if (name == null) return false
        return COMPATIBLE_PREFIXES.any { name.startsWith(it, ignoreCase = true) }
    }

    /**
     * Checks if device is specifically a MELK device requiring initialization.
     */
    fun isMelkDevice(name: String?): Boolean {
        return name?.startsWith(MELK_PREFIX, ignoreCase = true) == true
    }

    /**
     * Initializes a MELK device after connection.
     * CRITICAL: MELK devices ignore commands without this init sequence.
     *
     * @param writeCommand Function to execute a write (provided by TowerCommandQueue)
     */
    suspend fun initializeMelkDevice(writeCommand: suspend (ByteArray) -> Unit) {
        Timber.d("Initializing MELK device with handshake sequence")

        // Init command 1: 0x7e 0x07 0x83
        writeCommand(byteArrayOf(START_BYTE, 0x07, 0x83.toByte()))
        delay(50) // Allow device processing time

        // Init command 2: 0x7e 0x04 0x04
        writeCommand(byteArrayOf(START_BYTE, 0x04, 0x04))
        delay(50)

        Timber.d("MELK device initialization complete")
    }

    // ========== Power Commands ==========

    /**
     * Power ON command.
     * Command: 0x7e 0x04 0x04 0xf0 0x00 0x01 0xff 0x00 0xef
     */
    fun powerOn(): ByteArray = byteArrayOf(
        START_BYTE,
        0x04, 0x04,
        0xF0.toByte(), 0x00, 0x01,
        0xFF.toByte(), 0x00,
        END_BYTE
    )

    /**
     * Power OFF command.
     * Command: 0x7e 0x04 0x04 0x00 0x00 0x00 0xff 0x00 0xef
     */
    fun powerOff(): ByteArray = byteArrayOf(
        START_BYTE,
        0x04, 0x04,
        0x00, 0x00, 0x00,
        0xFF.toByte(), 0x00,
        END_BYTE
    )

    // ========== Color Command ==========

    /**
     * Set RGB color command.
     * Command format: 0x7e 0x07 0x05 0x03 R G B 0x10 0xef
     *
     * @param r Red value 0-255
     * @param g Green value 0-255
     * @param b Blue value 0-255
     */
    fun setColor(r: Int, g: Int, b: Int): ByteArray = byteArrayOf(
        START_BYTE,
        0x07, 0x05, 0x03,
        r.coerceIn(0, 255).toByte(),
        g.coerceIn(0, 255).toByte(),
        b.coerceIn(0, 255).toByte(),
        0x10,
        END_BYTE
    )

    // ========== Brightness Command ==========

    /**
     * Set brightness level command.
     * Command format: 0x7e 0x00 0x01 LEVEL 0x00 0x00 0x00 0x00 0xef
     *
     * @param level Brightness level 0-100
     */
    fun setBrightness(level: Int): ByteArray = byteArrayOf(
        START_BYTE,
        0x00, 0x01,
        level.coerceIn(0, 100).toByte(),
        0x00, 0x00, 0x00, 0x00,
        END_BYTE
    )

    // ========== Effect Commands ==========

    /**
     * Set hardware effect with speed.
     * Command format: 0x7e 0x00 0x03 EFFECT_ID SPEED 0x00 0x00 0x00 0xef
     *
     * @param effectId Effect ID byte from Effect enum
     * @param speed Effect playback speed 0-100 (0=slow, 100=fast)
     */
    fun setEffect(effectId: Byte, speed: Int): ByteArray = byteArrayOf(
        START_BYTE,
        0x00, 0x03,
        effectId,
        speed.coerceIn(0, 100).toByte(),
        0x00, 0x00, 0x00,
        END_BYTE
    )

    /**
     * Set effect speed only (for adjusting running effect).
     * Uses same format as setEffect.
     */
    fun setEffectSpeed(effectId: Byte, speed: Int): ByteArray = setEffect(effectId, speed)
}
