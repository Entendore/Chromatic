package com.example.chromatic.core.color

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt

object WcagContrast {
    const val AA_NORMAL = 4.5
    const val AA_LARGE = 3.0
    const val AAA_NORMAL = 7.0
    const val AAA_LARGE = 4.5
    const val UI_COMPONENTS = 3.0

    fun relativeLuminance(rgb: Rgb): Double {
        fun channel(value: Int): Double {
            val s = value / 255.0
            return if (s <= 0.03928) s / 12.92 else ((s + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * channel(rgb.red) + 0.7152 * channel(rgb.green) + 0.0722 * channel(rgb.blue)
    }

    fun contrastRatio(a: Rgb, b: Rgb): Double {
        val la = relativeLuminance(a)
        val lb = relativeLuminance(b)
        return (maxOf(la, lb) + 0.05) / (minOf(la, lb) + 0.05)
    }

    fun report(foreground: Rgb, background: Rgb) = ContrastReport(contrastRatio(foreground, background))

    /**
     * Always succeeds: the scan includes L=0 (black) and L=1 (white), and for any
     * background luminance at least one of black/white meets 4.5:1
     * (black when L_bg ≥ 0.175, white when L_bg ≤ 0.1833) — so the union covers
     * every possible background. A nullable return would lie about the domain.
     */
    fun adjustedForeground(foreground: Rgb, background: Rgb, targetRatio: Double = AA_NORMAL): Rgb {
        val original = foreground.toHsl()
        return (0..100).asSequence()
            .map { original.copy(lightness = it / 100f).toRgb() }
            .filter { contrastRatio(it, background) >= targetRatio }
            .minByOrNull { abs(it.toHsl().lightness - original.lightness) }!!
    }
}

data class ContrastReport(val ratio: Double) {
    val passesAaNormal get() = ratio >= WcagContrast.AA_NORMAL
    val passesAaLarge get() = ratio >= WcagContrast.AA_LARGE
    val passesAaaNormal get() = ratio >= WcagContrast.AAA_NORMAL
    val passesAaaLarge get() = ratio >= WcagContrast.AAA_LARGE
    val passesUiComponents get() = ratio >= WcagContrast.UI_COMPONENTS

    /** Two-decimal formatting without java.util.Locale (multiplatform, v11 §2a). */
    val formatted: String
        get() {
            val scaled = (ratio * 100).roundToInt()
            val whole = scaled / 100
            val frac = (scaled % 100).toString().padStart(2, '0')
            return "$whole.$frac:1"
        }
}
