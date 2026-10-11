package com.example.chromatic.desktop

import com.example.chromatic.core.color.PaletteStyle
import com.example.chromatic.core.color.PaletteStyles
import com.example.chromatic.core.color.Rgb
import com.example.chromatic.core.color.WcagContrast
import com.example.chromatic.core.color.toOkLab
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PlaygroundModelTest {

    @Test fun `colors respect their style envelope`() {
        val model = PlaygroundModel(PaletteStyle.JEWEL, 200.0, 3)
        model.colors.forEach { assertTrue(PlaygroundModel.envelopeCheck(PaletteStyle.JEWEL, it)) }
    }

    @Test fun `tones darken monotonically and dark levels verify`() {
        val model = PlaygroundModel(PaletteStyle.JEWEL, 120.0, 1)
        val lightness = model.tones.map { it.rgb.toOkLab().l }
        assertTrue(lightness.zipWithNext().all { (a, b) -> a > b })
        model.darkToneLevels.forEach { level ->
            val rgb = model.tones.first { it.level == level }.rgb
            assertTrue(WcagContrast.contrastRatio(Rgb.WHITE, rgb) >= WcagContrast.AA_NORMAL)
        }
    }

    @Test fun `deterministic per seed`() =
        assertEquals(
            PlaygroundModel(PaletteStyle.NEON, 45.0, 7).colors,
            PlaygroundModel(PaletteStyle.NEON, 45.0, 7).colors,
        )
}
