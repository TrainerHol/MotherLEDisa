package com.motherledisa.domain.animation

import androidx.core.graphics.ColorUtils

/**
 * HSV color interpolation for smooth transitions.
 * Per D-08: HSV interpolation for red->orange->yellow->green flow.
 */
object ColorInterpolator {

    /**
     * Interpolate between two colors using HSL (handles hue wrapping).
     * @param color1 Start color as ARGB int
     * @param color2 End color as ARGB int
     * @param fraction Progress 0.0 to 1.0
     * @return Interpolated color as ARGB int
     */
    fun interpolateHSV(color1: Int, color2: Int, fraction: Float): Int {
        val hsl1 = FloatArray(3)
        val hsl2 = FloatArray(3)

        ColorUtils.colorToHSL(color1, hsl1)
        ColorUtils.colorToHSL(color2, hsl2)

        // Handle hue wrapping (shortest path around color wheel)
        // Per RESEARCH.md Pattern 2: prevents red->purple going through green
        var hueDiff = hsl2[0] - hsl1[0]
        if (hueDiff > 180) hueDiff -= 360
        if (hueDiff < -180) hueDiff += 360

        val resultHsl = floatArrayOf(
            (hsl1[0] + hueDiff * fraction + 360) % 360,  // Hue with wrap
            hsl1[1] + (hsl2[1] - hsl1[1]) * fraction,     // Saturation
            hsl1[2] + (hsl2[2] - hsl1[2]) * fraction      // Lightness
        )

        return ColorUtils.HSLToColor(resultHsl)
    }

    /**
     * Linear interpolation for brightness.
     */
    fun interpolateBrightness(start: Int, end: Int, fraction: Float): Int =
        (start + (end - start) * fraction).toInt().coerceIn(0, 100)
}
