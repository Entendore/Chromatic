package com.example.chromatic.core.color

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RgbTest {
    @Test fun `hex parses six digit and shorthand`() {
        assertEquals(Rgb(255, 87, 34), Rgb.fromHex("#FF5722"))
        assertEquals(Rgb(170, 187, 204), Rgb.fromHex("#ABC"))
        assertEquals(Rgb.fromHex("#00f"), Rgb.fromHex("#0000FF"))
    }

    @Test fun `hex round-trips for sampled values`() {
        repeat(5_000) { i ->
            val v = i * 2654435761
            val rgb = Rgb((v shr 16) and 0xFF, (v shr 8) and 0xFF, v and 0xFF)
            assertEquals(rgb, Rgb.fromHex(rgb.hex))
        }
    }

    @Test fun `known formats match the legacy Locale output`() {
        assertEquals("#FF5722", Rgb(255, 87, 34).hex)
        assertEquals("#000000", Rgb.BLACK.hex)
        assertEquals("#FFFFFF", Rgb.WHITE.hex)
    }
}

class ColorConverterTest {
    @Test fun `pure red converts to hsv and hsl`() {
        val hsv = Rgb(255, 0, 0).toHsv()
        assertEquals(0f, hsv.hue, 0.01f); assertEquals(1f, hsv.saturation, 0.01f); assertEquals(1f, hsv.value, 0.01f)
        assertEquals(0.5f, Rgb(255, 0, 0).toHsl().lightness, 1e-3f)
    }

    @Test fun `hsv round trip preserves color`() {
        listOf(Hsv(0f, 0.5f, 0.5f), Hsv(37f, 0.8f, 0.9f), Hsv(200f, 1f, 0.3f), Hsv(320f, 0.25f, 1f))
            .forEach {
                val back = it.toRgb().toHsv()
                assertEquals(it.hue, back.hue, 0.05f)
                assertEquals(it.saturation, back.saturation, 0.05f)
                assertEquals(it.value, back.value, 0.05f)
            }
    }

    @Test fun `hue normalizes out of range values`() {
        assertEquals(30f, ColorConverter.normalizeHue(390f))
        assertEquals(330f, ColorConverter.normalizeHue(-30f))
    }
}

class OkLabTest {
    @Test fun `white has L 1 and black has L 0`() {
        assertEquals(1.0, Rgb.WHITE.toOkLab().l, 1e-3)
        assertEquals(0.0, Rgb.BLACK.toOkLab().l, 1e-3)
    }

    @Test fun `round trip is stable for in-gamut colors`() {
        val color = Rgb(90, 140, 210)
        val back = color.toOkLab().toRgb()
        assertTrue(abs(back.red - color.red) <= 1)
        assertTrue(abs(back.green - color.green) <= 1)
        assertTrue(abs(back.blue - color.blue) <= 1)
    }

    @Test fun `deltaE grows with perceptual distance`() {
        assertTrue(deltaEOkLab(Rgb.BLACK, Rgb.WHITE) > deltaEOkLab(Rgb.BLACK, Rgb(128, 128, 128)))
    }
}

class WcagContrastTest {
    @Test fun `black on white is exactly 21 to 1`() =
        assertEquals(21.0, WcagContrast.contrastRatio(Rgb.BLACK, Rgb.WHITE), 0.01)

    @Test fun `pure red luminance matches the spec`() =
        assertEquals(0.2126, WcagContrast.relativeLuminance(Rgb(255, 0, 0)), 1e-4)

    @Test fun `contrast ratio is symmetric`() {
        val a = Rgb(30, 144, 255); val b = Rgb(250, 128, 114)
        assertEquals(WcagContrast.contrastRatio(a, b), WcagContrast.contrastRatio(b, a), 1e-9)
    }

    @Test fun `formatted output matches the legacy two-decimal shape`() {
        assertEquals("21.00:1", WcagContrast.report(Rgb.BLACK, Rgb.WHITE).formatted)
        assertEquals(4.48, WcagContrast.contrastRatio(Rgb(119, 119, 119), Rgb.WHITE), 0.02)
    }

    @Test fun `adjustedForeground always succeeds across the background range (v12 proof)`() {
        (0 until 32).forEach { i ->
            val l = i / 31.0
            val hue = (i * 37.0) % 360.0
            val bg = OkLch(l, GamutMapper.maxChroma(l, hue) * 0.7, hue).toRgbClamped()
            val fg = OkLch(0.5, 0.06, (hue + 180.0) % 360.0).toRgbClamped()
            val fixed = WcagContrast.adjustedForeground(fg, bg)
            assertTrue(
                "bg L=${l}",
                WcagContrast.contrastRatio(fixed, bg) >= WcagContrast.AA_NORMAL,
            )
        }
    }

    @Test fun `fix stays near the original lightness when already passing`() {
        val fg = Rgb(60, 60, 60)
        val fixed = WcagContrast.adjustedForeground(fg, Rgb.WHITE)
        assertTrue(abs(fixed.toHsl().lightness - fg.toHsl().lightness) < 0.02)
    }
}

class ApcaContrastTest {
    @Test fun `black on white is about 106 Lc`() {
        val lc = ApcaContrast.lightnessContrast(Rgb.BLACK, Rgb.WHITE)
        assertTrue(lc in 104.0..108.0)
    }

    @Test fun `polarity is negative for light text on dark background`() {
        val lc = ApcaContrast.lightnessContrast(Rgb.WHITE, Rgb.BLACK)
        assertTrue(abs(lc) in 104.0..110.0)
        assertTrue(lc < 0)
    }
}

class ColorBlenderTest {
    @Test fun `oklab midpoint of black and white is neutral gray`() {
        val mid = ColorBlender.blend(Rgb.BLACK, Rgb.WHITE, 0.5f, BlendSpace.OKLAB)
        assertEquals(mid.red, mid.green)
        assertEquals(mid.green, mid.blue)
    }

    @Test fun `linear midpoint is much brighter than srgb midpoint`() {
        val srgb = ColorBlender.blend(Rgb.BLACK, Rgb.WHITE, 0.5f, BlendSpace.SRGB)
        val linear = ColorBlender.blend(Rgb.BLACK, Rgb.WHITE, 0.5f, BlendSpace.LINEAR_SRGB)
        assertTrue(linear.red > srgb.red + 40)
    }

    @Test fun `ramp includes both endpoints`() {
        val ramp = ColorBlender.ramp(Rgb(255, 0, 0), Rgb(0, 0, 255), 5, BlendSpace.OKLAB)
        assertEquals(5, ramp.size)
        assertEquals(Rgb(255, 0, 0), ramp.first())
        assertEquals(Rgb(0, 0, 255), ramp.last())
    }
}

class MixersTest {
    @Test fun `adding red and green light makes yellow`() =
        assertEquals(Rgb(255, 255, 0), LightMixer.add(Rgb(255, 0, 0), Rgb(0, 255, 0)))

    @Test fun `adding complementary light makes white`() =
        assertEquals(Rgb(255, 255, 255), LightMixer.add(Rgb(255, 0, 0), Rgb(0, 255, 255)))

    @Test fun `mixing complementary pigment makes black`() {
        val mixed = SubtractiveMixer.mix(Rgb(255, 0, 0), Rgb(0, 255, 255), 0.5f)
        assertTrue(mixed.red < 30 && mixed.green < 30 && mixed.blue < 30)
    }

    @Test fun `endpoints are exact through the exponent path (mutation survivor, v15)`() {
        val a = Rgb(200, 100, 50); val b = Rgb(10, 10, 200)
        assertEquals(a, SubtractiveMixer.mix(a, b, 0f))
        assertEquals(b, SubtractiveMixer.mix(a, b, 1f))
    }
}

class ColorNamerTest {
    @Test fun `identifies pure red and white`() {
        assertEquals("Red", ColorNamer.nameOf(Rgb(255, 0, 0)))
        assertEquals("White", ColorNamer.nameOf(Rgb.WHITE))
    }

    @Test fun `named entry matches its canonical color`() {
        val named = ColorNamer.namePool().first { it == "Sky Blue" }
        val lab = ColorNamer.hueOf(named)!!
        assertTrue(lab in 180.0..230.0)
    }
}

class ColorOfDayTest {
    @Test fun `same epoch day yields the same color`() =
        assertEquals(ColorOfDay.forEpochDay(20_000), ColorOfDay.forEpochDay(20_000))

    @Test fun `adjacent days differ`() {
        repeat(50) { d -> assertTrue(ColorOfDay.forEpochDay(d.toLong()) != ColorOfDay.forEpochDay(d + 1L)) }
    }

    @Test fun `today derives from the supplied kotlinx date`() {
        val date = kotlinx.datetime.LocalDate.parse("2025-03-14")
        assertEquals(ColorOfDay.forEpochDay(date.toEpochDays().toLong()), ColorOfDay.today(date))
    }
}

class ContrastGridTest {
    private val tones = listOf(
        PaletteFactory.Tone(0, Rgb.BLACK),
        PaletteFactory.Tone(1, Rgb.WHITE),
        PaletteFactory.Tone(2, Rgb(119, 119, 119)),
    )

    @Test fun `black on white is 21 to 1`() =
        assertEquals(21.0, ContrastGrid.ratios(tones)[0][1], 0.01)

    @Test fun `diagonal is always 1 to 1`() {
        val grid = ContrastGrid.ratios(tones)
        tones.indices.forEach { i -> assertEquals(1.0, grid[i][i], 1e-9) }
    }

    @Test fun `precomputed grid matches pairwise ratios (v9 regression)`() {
        val scale = PaletteFactory.tonalScale(Rgb(120, 60, 200))
        val grid = ContrastGrid.ratios(scale)
        scale.indices.forEach { r ->
            scale.indices.forEach { c ->
                assertEquals(WcagContrast.contrastRatio(scale[r].rgb, scale[c].rgb), grid[r][c], 1e-9)
            }
        }
    }

    @Test fun `grade thresholds follow the wcag spec`() {
        assertEquals(ContrastGrid.Grade.FAIL, ContrastGrid.grade(2.9))
        assertEquals(ContrastGrid.Grade.LARGE, ContrastGrid.grade(3.0))
        assertEquals(ContrastGrid.Grade.AA, ContrastGrid.grade(4.5))
        assertEquals(ContrastGrid.Grade.AAA, ContrastGrid.grade(7.0))
    }
}
