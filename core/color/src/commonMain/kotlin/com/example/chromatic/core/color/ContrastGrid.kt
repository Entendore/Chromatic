package com.example.chromatic.core.color

/** All pairwise WCAG ratios of a tonal scale, plus a simple 4-tier grade. */
object ContrastGrid {

    enum class Grade(val passesBodyText: Boolean) {
        FAIL(false), LARGE(false), AA(true), AAA(true),
    }

    fun grade(ratio: Double): Grade = when {
        ratio >= WcagContrast.AAA_NORMAL -> Grade.AAA
        ratio >= WcagContrast.AA_NORMAL -> Grade.AA
        ratio >= WcagContrast.AA_LARGE -> Grade.LARGE
        else -> Grade.FAIL
    }

    /** [row][col] = ratio of row-color as text on column-color as background.
     *  Luminances computed once (v9) — not twice per pair. */
    fun ratios(tones: List<PaletteFactory.Tone>): List<List<Double>> {
        val lum = DoubleArray(tones.size) { WcagContrast.relativeLuminance(tones[it].rgb) }
        return tones.indices.map { r ->
            tones.indices.map { c ->
                val hi = maxOf(lum[r], lum[c]); val lo = minOf(lum[r], lum[c])
                (hi + 0.05) / (lo + 0.05)
            }
        }
    }
}
