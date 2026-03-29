package com.motherledisa.domain.model

/**
 * Sound-reactive effects supported by tower's built-in microphone.
 * Effect IDs 0x80-0x87 from ELK-BLEDOM protocol.
 * Per D-01: Tower handles audio detection in hardware.
 */
enum class SoundEffect(val id: Byte, val displayName: String) {
    ENERGETIC(0x80.toByte(), "Energetic"),
    PULSE(0x81.toByte(), "Pulse"),
    FADE(0x82.toByte(), "Fade"),
    JUMP(0x83.toByte(), "Jump"),
    FLOW(0x84.toByte(), "Flow"),
    STROBE(0x85.toByte(), "Strobe"),
    RAINBOW(0x86.toByte(), "Rainbow"),
    WAVE(0x87.toByte(), "Wave");

    companion object {
        val all: List<SoundEffect> = entries.toList()

        fun fromId(id: Byte): SoundEffect? = entries.find { it.id == id }
    }
}
