package com.motherledisa.data.ble

/**
 * Sealed class hierarchy for BLE commands sent through the command queue.
 * Each command type carries its serialized byte payload.
 */
sealed class BleCommand {
    abstract val data: ByteArray

    /** Power on/off command */
    data class Power(override val data: ByteArray) : BleCommand() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Power) return false
            return data.contentEquals(other.data)
        }

        override fun hashCode(): Int = data.contentHashCode()
    }

    /** Set RGB color command */
    data class SetColor(override val data: ByteArray) : BleCommand() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is SetColor) return false
            return data.contentEquals(other.data)
        }

        override fun hashCode(): Int = data.contentHashCode()
    }

    /** Set brightness level command */
    data class SetBrightness(override val data: ByteArray) : BleCommand() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is SetBrightness) return false
            return data.contentEquals(other.data)
        }

        override fun hashCode(): Int = data.contentHashCode()
    }

    /** Set hardware effect command */
    data class SetEffect(override val data: ByteArray) : BleCommand() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is SetEffect) return false
            return data.contentEquals(other.data)
        }

        override fun hashCode(): Int = data.contentHashCode()
    }

    /** Raw initialization command (for MELK handshake) */
    data class Initialize(override val data: ByteArray) : BleCommand() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Initialize) return false
            return data.contentEquals(other.data)
        }

        override fun hashCode(): Int = data.contentHashCode()
    }
}
