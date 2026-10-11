package com.example.chromatic.core.color

import kotlin.math.pow

/**
 * APCA 0.1.9 — the perceived-contrast method proposed for WCAG 3.
 * Lc ∈ ~[-108, +108]; sign encodes polarity (dark-on-light positive).
 */
object ApcaContrast {
    private const val BLACK_THRESHOLD = 0.022
    private const val BLACK_CLAMP = 1.414

    fun lightnessContrast(text: Rgb, background: Rgb): Double {
        val yText = screenLuminance(text)
        val yBg = screenLuminance(background)
        return if (yBg > yText) {
            val raw = (yBg.pow(0.56) - yText.pow(0.57)) * 1.14
            if (raw < 0.1) 0.0 else (raw - 0.027) * 100.0
        } else {
            val raw = (yText.pow(0.62) - yBg.pow(0.65)) * 1.14
            if (raw < 0.1) 0.0 else -((raw - 0.027) * 100.0)
        }
    }

    private fun screenLuminance(rgb: Rgb): Double {
        fun channel(value: Int): Double {
            val y = (value / 255.0).pow(2.4)
            return if (y < BLACK_THRESHOLD) y + (BLACK_THRESHOLD - y).pow(BLACK_CLAMP) else y
        }
        return 0.2126729 * channel(rgb.red) + 0.7151522 * channel(rgb.green) + 0.0721750 * channel(rgb.blue)
    }
}
