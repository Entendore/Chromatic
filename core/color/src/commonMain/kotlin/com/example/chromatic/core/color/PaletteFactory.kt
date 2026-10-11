package com.example.chromatic.core.color

/** Material-style tonal scales in OKLCH — perceptually even lightness steps. */
object PaletteFactory {

    data class Tone(val level: Int, val rgb: Rgb)

    private val LEVELS = listOf(50, 100, 200, 300, 400, 500, 600, 700, 800, 900)
    private val TARGET_LIGHTNESS =
        listOf(0.955, 0.91, 0.81, 0.71, 0.61, 0.50, 0.41, 0.31, 0.21, 0.12)

    fun tonalScale(base: Rgb): List<Tone> {
        val seed = base.toOkLch()
        val maxAtSeed = GamutMapper.maxChroma(seed.lightness, seed.hueDegrees)
        val relativeChroma =
            if (maxAtSeed < 1e-6 || seed.chroma < 0.01) 0.0
            else (seed.chroma / maxAtSeed).coerceIn(0.30, 1.0)

        return LEVELS.zip(TARGET_LIGHTNESS) { level, l ->
            val chroma = GamutMapper.maxChromaCached(l, seed.hueDegrees) * relativeChroma
            Tone(level, OkLch(l, chroma, seed.hueDegrees).toRgbClamped())
        }
    }
}
