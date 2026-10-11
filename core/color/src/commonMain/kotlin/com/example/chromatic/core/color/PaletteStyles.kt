package com.example.chromatic.core.color

import kotlin.random.Random

enum class PaletteStyle { PASTEL, NEON, EARTH, JEWEL, MONOCHROME, VINTAGE }

object PaletteStyles {

    data class Range(val lightness: ClosedFloatingPointRange<Double>, val chroma: ClosedFloatingPointRange<Double>)

    private val RANGES = mapOf(
        PaletteStyle.PASTEL to Range(0.82..0.93, 0.03..0.09),
        PaletteStyle.NEON to Range(0.70..0.88, 0.18..0.33),
        PaletteStyle.EARTH to Range(0.35..0.75, 0.04..0.11),
        PaletteStyle.JEWEL to Range(0.35..0.55, 0.13..0.24),
        PaletteStyle.MONOCHROME to Range(0.20..0.95, 0.0..0.02),
        PaletteStyle.VINTAGE to Range(0.55..0.80, 0.05..0.11),
    )

    fun rangeOf(style: PaletteStyle): Range = RANGES.getValue(style)

    private fun lerp(a: Double, b: Double, t: Double) = a + (b - a) * t

    fun colorInStyle(style: PaletteStyle, hue: Double, random: Random): Rgb {
        val range = rangeOf(style)
        val l = lerp(range.lightness.start, range.lightness.endInclusive, random.nextDouble())
        val target = lerp(range.chroma.start, range.chroma.endInclusive, random.nextDouble())
        val c = GamutMapper.maxChroma(l, hue).coerceAtMost(target)
        return OkLch(l, c, hue).toRgbClamped()
    }

    fun palette(style: PaletteStyle, count: Int = 5, random: Random = Random.Default): List<Rgb> {
        val anchor = random.nextDouble() * 360.0
        val spread = if (style == PaletteStyle.MONOCHROME) 8.0 else 130.0
        return List(count) { i ->
            val hue = (anchor + spread * i / (count - 1).coerceAtLeast(1)
                + (random.nextDouble() - 0.5) * 24.0 + 360.0) % 360.0
            colorInStyle(style, hue, random)
        }
    }
}
