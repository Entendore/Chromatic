package com.example.chromatic.core.color

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.random.Random

class GamutMapperTest {
    @Test fun `in-gamut colors pass through unchanged`() {
        val color = OkLch(0.5, 0.08, 120.0)
        assertEquals(0.0, deltaEOkLab(color.toRgb(), color.toRgbClamped()), 1e-9)
    }

    @Test fun `mapping trades chroma but keeps lightness and hue`() {
        val wild = OkLch(0.92, 0.32, 45.0)
        val mapped = wild.toRgbClamped().toOkLch()
        assertTrue(abs(mapped.lightness - wild.lightness) < 0.02)
        assertTrue(abs(mapped.hueDegrees - wild.hueDegrees) < 4.0)
        assertTrue(mapped.chroma < wild.chroma)
    }

    @Test fun `max chroma peaks at mid lightness`() =
        assertTrue(GamutMapper.maxChroma(0.5, 150.0) > GamutMapper.maxChroma(0.98, 150.0))

    @Test fun `warm-start cache returns identical values to the cold search (v10 regression)`() {
        (0 until 64).forEach { i ->
            val l = i / 64.0; val h = (i * 11.0) % 360.0
            assertEquals(GamutMapper.maxChroma(l, h), GamutMapper.maxChromaCached(l, h), GamutMapper.TOLERANCE)
        }
    }

    @Test fun `cached results stay in gamut after warm path`() {
        (0 until 64).forEach { i ->
            val l = 0.05 + i / 64.0 * 0.9; val h = (i * 37.0) % 360.0
            val rgb = OkLch(l, GamutMapper.maxChromaCached(l, h), h).toRgbClamped()
            assertTrue(GamutMapper.inGamut(rgb.toOkLab()))
        }
    }
}

class PaletteFactoryTest {
    @Test fun `gray stays neutral at every tone`() {
        PaletteFactory.tonalScale(Rgb(128, 128, 128)).forEach { tone ->
            assertEquals(tone.rgb.red, tone.rgb.green)
            assertEquals(tone.rgb.green, tone.rgb.blue)
        }
    }

    @Test fun `hue survives across the scale for saturated colors`() {
        val base = Rgb(30, 90, 220)
        val hue = base.toOkLch().hueDegrees
        PaletteFactory.tonalScale(base)
            .filter { it.level in 200..700 }
            .forEach { assertTrue("level ${it.level}", abs(it.rgb.toOkLch().hueDegrees - hue) < 3.0) }
    }

    @Test fun `tones get monotonically darker`() {
        val lightness = PaletteFactory.tonalScale(Rgb(200, 40, 90)).map { it.rgb.toOkLab().l }
        assertTrue(lightness.zipWithNext().all { (a, b) -> a > b })
    }

    @Test fun `tonal scale is unchanged by the cache`() {
        val base = Rgb(30, 90, 220)
        assertEquals(PaletteFactory.tonalScale(base), PaletteFactory.tonalScale(base))
    }
}

class ColorHarmonyTest {
    private val base = Rgb(90, 140, 210).toHsv()

    @Test fun `complementary sits 180 degrees away`() {
        val pair = ColorHarmony.COMPLEMENTARY.generate(Hsv(20f, 0.5f, 1f))
        assertEquals(200f, pair[1].hue, 0.01f)
    }

    @Test fun `triadic hues are evenly spaced traditionally`() {
        val hues = ColorHarmony.TRIADIC.generate(Hsv(0f, 1f, 1f)).map { it.hue }
        assertEquals(0f, hues[0], 0.01f); assertEquals(120f, hues[1], 0.01f); assertEquals(240f, hues[2], 0.01f)
    }

    @Test fun `oklch triadic hues sit 120 degrees apart in oklch space`() {
        val hues = ColorHarmony.TRIADIC.generate(base, HarmonyMode.OKLCH).map { it.toRgb().toOkLch().hueDegrees }
        assertEquals(120.0, (hues[1] - hues[0] + 360.0) % 360.0, 1.0)
        assertEquals(240.0, (hues[2] - hues[0] + 360.0) % 360.0, 1.0)
    }

    @Test fun `the two modes genuinely differ`() {
        val traditional = ColorHarmony.COMPLEMENTARY.generate(base, HarmonyMode.TRADITIONAL)[1]
        val oklch = ColorHarmony.COMPLEMENTARY.generate(base, HarmonyMode.OKLCH)[1]
        assertTrue(deltaEOkLab(traditional.toRgb(), oklch.toRgb()) > 0.005)
    }

    @Test fun `monochromatic ignores hue mode`() =
        assertEquals(
            ColorHarmony.MONOCHROMATIC.generate(base, HarmonyMode.TRADITIONAL),
            ColorHarmony.MONOCHROMATIC.generate(base, HarmonyMode.OKLCH),
        )
}

class PaletteStylesTest {
    @Test fun `every color respects its envelope and stays in gamut`() {
        PaletteStyle.entries.forEach { style ->
            val range = PaletteStyles.rangeOf(style)
            repeat(20) { seed ->
                PaletteStyles.palette(style, random = Random(seed.toLong())).forEach { rgb ->
                    val lch = rgb.toOkLch()
                    assertTrue(lch.lightness in range.lightness)
                    assertTrue(lch.chroma <= GamutMapper.maxChroma(lch.lightness, lch.hueDegrees) + 1e-6)
                }
            }
        }
    }

    @Test fun `pastel is far duller than neon`() {
        fun meanChroma(style: PaletteStyle) =
            List(20) { PaletteStyles.palette(style, random = Random(it.toLong())) }
                .flatten().map { it.toOkLch().chroma }.average()
        assertTrue(meanChroma(PaletteStyle.PASTEL) * 1.5 < meanChroma(PaletteStyle.NEON))
    }

    @Test fun `generation is deterministic per seed`() =
        assertEquals(
            PaletteStyles.palette(PaletteStyle.JEWEL, random = Random(9)),
            PaletteStyles.palette(PaletteStyle.JEWEL, random = Random(9)),
        )
}

class PaletteExtractorTest {
    @Test fun `recovers three well separated clusters`() {
        val rng = Random(7)
        fun jitter(c: Rgb) = Rgb(
            (c.red + rng.nextInt(-10, 11)).coerceIn(0, 255),
            (c.green + rng.nextInt(-10, 11)).coerceIn(0, 255),
            (c.blue + rng.nextInt(-10, 11)).coerceIn(0, 255),
        )
        val pixels = List(40) { jitter(Rgb(220, 30, 30)) } +
            List(40) { jitter(Rgb(30, 200, 60)) } +
            List(40) { jitter(Rgb(40, 60, 220)) }

        val swatches = PaletteExtractor.extract(pixels, clusterCount = 3, seed = 42)
        assertEquals(3, swatches.size)
        swatches.forEach { assertEquals(1.0 / 3, it.fraction, 0.1) }
        // ΔE spans ~0–1 (v3 fix: impossible >30 threshold corrected)
        assertTrue(deltaEOkLab(swatches[0].rgb, swatches[1].rgb) > 0.3)
        assertTrue(deltaEOkLab(swatches[1].rgb, swatches[2].rgb) > 0.3)
    }
}

class CvdSafePaletteTest {
    @Test fun `improves red green separation under deuteranopia`() {
        val original = listOf(Rgb(220, 40, 40), Rgb(50, 180, 60))
        val before = original.map { ColorVisionSimulator.simulate(it, ColorVisionType.DEUTERANOPIA) }
        val beforeMin = deltaEOkLab(before[0], before[1])

        val result = CvdSafePalette.makeDistinguishable(original, ColorVisionType.DEUTERANOPIA)

        assertTrue(result.minSimulatedDeltaE > beforeMin)
        assertTrue(result.adjustmentsMade > 0)
        result.colors.zip(original).forEach { (adjusted, seed) ->
            assertEquals(seed.toHsv().saturation, adjusted.toHsv().saturation, 1e-4)
            assertEquals(seed.toHsv().value, adjusted.toHsv().value, 1e-4)
        }
    }
}

class SrgbMatrixTest {
    @Test fun `every channel row sums to one so white maps to white`() {
        ColorVisionType.entries.filter { it != ColorVisionType.NORMAL }.forEach { type ->
            val m = ColorVisionSimulator.srgbMatrix(type)!!
            assertEquals(20, m.size)
            listOf(0, 1, 2).forEach { row ->
                assertEquals(1.0, m.slice(row * 5 until row * 5 + 3).sum().toDouble(), 1e-3)
            }
            assertEquals(1f, m[15])
        }
    }

    @Test fun `normal vision needs no preview matrix`() =
        assertEquals(null, ColorVisionSimulator.srgbMatrix(ColorVisionType.NORMAL))
}

class ThemeGeneratorTest {
    private val bundle = ThemeGenerator.generate(Rgb(190, 60, 80))

    @Test fun `every verified pair passes AA in both modes`() {
        (bundle.light.verifications + bundle.dark.verifications).forEach {
            assertTrue("${it.label} = ${it.ratio}", it.passes)
        }
    }

    @Test fun `exports pasteable compose code`() {
        val code = ThemeGenerator.toComposeCode(bundle, "Brand")
        assertTrue(code.contains("BrandLightColors = lightColorScheme("))
        assertTrue(code.contains("primary = Color(0x"))
        assertTrue(code.contains("BrandDarkColors = darkColorScheme("))
    }
}

class PaletteExporterTest {
    @Test fun `css uses sanitized kebab names`() {
        val css = PaletteExporter.export("My Cool Palette!", listOf(Rgb(255, 0, 0)), ExportFormat.CSS)
        assertTrue(css.contains("--my-cool-palette-1: #ff0000;"))
    }

    @Test fun `xml produces color resources`() {
        val xml = PaletteExporter.export("Brand", listOf(Rgb(0, 0, 255)), ExportFormat.ANDROID_XML)
        assertTrue(xml.contains("<color name=\"brand_1\">#0000FF</color>"))
    }

    @Test fun `compose output is valid ARGB literal`() {
        val kt = PaletteExporter.export("App", listOf(Rgb(255, 87, 34)), ExportFormat.COMPOSE)
        assertTrue(kt.contains("Color(0xFFFF5722FF)"))
    }

    @Test fun `css gradient declares interpolation space`() {
        val css = PaletteExporter.cssLinearGradient(Rgb.BLACK, Rgb.WHITE, BlendSpace.OKLAB)
        assertEquals("linear-gradient(in oklab, #000000, #ffffff)", css)
    }
}
