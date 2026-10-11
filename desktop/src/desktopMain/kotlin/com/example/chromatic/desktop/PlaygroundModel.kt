package com.example.chromatic.desktop

import com.example.chromatic.core.color.GamutMapper
import com.example.chromatic.core.color.OkLch
import com.example.chromatic.core.color.PaletteFactory
import com.example.chromatic.core.color.PaletteStyle
import com.example.chromatic.core.color.PaletteStyles
import com.example.chromatic.core.color.Rgb
import com.example.chromatic.core.color.WcagContrast
import com.example.chromatic.core.color.toOkLch
import kotlin.random.Random

/** Pure derivation behind the desktop studio — testable, Kover-measurable (v10 §2). */
class PlaygroundModel(style: PaletteStyle, anchorHue: Double, seed: Int) {
    val colors: List<Rgb> = run {
        val rng = Random(seed)
        List(5) { i -> PaletteStyles.colorInStyle(style, (anchorHue + i * 24.0) % 360.0, rng) }
    }
    val tones: List<PaletteFactory.Tone> = PaletteFactory.tonalScale(colors.first())

    /** Tone levels dark enough for white text — drives the legend chip. */
    val darkToneLevels: List<Int> =
        tones.filter { WcagContrast.contrastRatio(Rgb.WHITE, it.rgb) >= WcagContrast.AA_NORMAL }
            .map { it.level }

    companion object {
        fun envelopeCheck(style: PaletteStyle, rgb: Rgb): Boolean {
            val range = PaletteStyles.rangeOf(style)
            val lch = rgb.toOkLch()
            return lch.lightness in range.lightness &&
                lch.chroma <= GamutMapper.maxChroma(lch.lightness, lch.hueDegrees) + OkLch(0.0, 1e-9, 0.0).chroma
        }
    }
}
