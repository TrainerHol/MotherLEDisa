package com.motherledisa.domain.model

/**
 * Effect categories for grouping in UI (D-22: effects grouped by category).
 */
enum class EffectCategory(val displayName: String) {
    STATIC("Static"),
    FADE("Fades"),
    JUMP("Jumps"),
    BREATHE("Breathing"),
    STROBE("Strobe"),
    MULTI_COLOR("Multi-Color"),
    GRADIENT("Gradients")
}

/**
 * Represents a built-in hardware effect from the ELK-BLEDOM protocol.
 * D-18: Full hardware effects menu exposed (~20+ effects).
 */
data class Effect(
    /** Effect ID sent to device (protocol byte value) */
    val id: Byte,
    /** Human-readable effect name */
    val name: String,
    /** Category for grouping (D-22) */
    val category: EffectCategory
)

/**
 * Complete list of ELK-BLEDOM hardware effects.
 * Documented from protocol specification and Home Assistant integration.
 */
object AllEffects {
    // Static modes (single color)
    val RED_STATIC = Effect(0x80.toByte(), "Red", EffectCategory.STATIC)
    val GREEN_STATIC = Effect(0x81.toByte(), "Green", EffectCategory.STATIC)
    val BLUE_STATIC = Effect(0x82.toByte(), "Blue", EffectCategory.STATIC)
    val YELLOW_STATIC = Effect(0x83.toByte(), "Yellow", EffectCategory.STATIC)
    val CYAN_STATIC = Effect(0x84.toByte(), "Cyan", EffectCategory.STATIC)
    val PURPLE_STATIC = Effect(0x85.toByte(), "Purple", EffectCategory.STATIC)
    val WHITE_STATIC = Effect(0x86.toByte(), "White", EffectCategory.STATIC)

    // Jump effects (quick color transitions)
    val THREE_COLOR_JUMP = Effect(0x87.toByte(), "3-Color Jump", EffectCategory.JUMP)
    val SEVEN_COLOR_JUMP = Effect(0x88.toByte(), "7-Color Jump", EffectCategory.JUMP)

    // Fade effects (smooth transitions)
    val THREE_COLOR_FADE = Effect(0x89.toByte(), "3-Color Fade", EffectCategory.FADE)
    val SEVEN_COLOR_FADE = Effect(0x8A.toByte(), "7-Color Fade", EffectCategory.FADE)
    val RED_FADE = Effect(0x8B.toByte(), "Red Fade", EffectCategory.FADE)
    val GREEN_FADE = Effect(0x8C.toByte(), "Green Fade", EffectCategory.FADE)
    val BLUE_FADE = Effect(0x8D.toByte(), "Blue Fade", EffectCategory.FADE)
    val YELLOW_FADE = Effect(0x8E.toByte(), "Yellow Fade", EffectCategory.FADE)
    val CYAN_FADE = Effect(0x8F.toByte(), "Cyan Fade", EffectCategory.FADE)
    val PURPLE_FADE = Effect(0x90.toByte(), "Purple Fade", EffectCategory.FADE)
    val WHITE_FADE = Effect(0x91.toByte(), "White Fade", EffectCategory.FADE)
    val RED_GREEN_FADE = Effect(0x92.toByte(), "Red-Green Fade", EffectCategory.FADE)
    val RED_BLUE_FADE = Effect(0x93.toByte(), "Red-Blue Fade", EffectCategory.FADE)
    val GREEN_BLUE_FADE = Effect(0x94.toByte(), "Green-Blue Fade", EffectCategory.FADE)

    // Strobe/flash effects
    val SEVEN_COLOR_STROBE = Effect(0x95.toByte(), "7-Color Strobe", EffectCategory.STROBE)
    val RED_STROBE = Effect(0x96.toByte(), "Red Strobe", EffectCategory.STROBE)
    val GREEN_STROBE = Effect(0x97.toByte(), "Green Strobe", EffectCategory.STROBE)
    val BLUE_STROBE = Effect(0x98.toByte(), "Blue Strobe", EffectCategory.STROBE)
    val YELLOW_STROBE = Effect(0x99.toByte(), "Yellow Strobe", EffectCategory.STROBE)
    val CYAN_STROBE = Effect(0x9A.toByte(), "Cyan Strobe", EffectCategory.STROBE)
    val PURPLE_STROBE = Effect(0x9B.toByte(), "Purple Strobe", EffectCategory.STROBE)
    val WHITE_STROBE = Effect(0x9C.toByte(), "White Strobe", EffectCategory.STROBE)

    /** All effects in display order */
    val all: List<Effect> = listOf(
        // Fades
        THREE_COLOR_FADE, SEVEN_COLOR_FADE,
        RED_FADE, GREEN_FADE, BLUE_FADE, YELLOW_FADE, CYAN_FADE, PURPLE_FADE, WHITE_FADE,
        RED_GREEN_FADE, RED_BLUE_FADE, GREEN_BLUE_FADE,
        // Jumps
        THREE_COLOR_JUMP, SEVEN_COLOR_JUMP,
        // Strobe
        SEVEN_COLOR_STROBE,
        RED_STROBE, GREEN_STROBE, BLUE_STROBE, YELLOW_STROBE, CYAN_STROBE, PURPLE_STROBE, WHITE_STROBE,
        // Static (less commonly used as effect, but available)
        RED_STATIC, GREEN_STATIC, BLUE_STATIC, YELLOW_STATIC, CYAN_STATIC, PURPLE_STATIC, WHITE_STATIC
    )

    /** Effects grouped by category for D-22 section headers */
    val byCategory: Map<EffectCategory, List<Effect>> = all.groupBy { it.category }
}
