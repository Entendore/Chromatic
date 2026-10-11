package com.example.chromatic.core.color

/** How hue offsets are measured (display labels live in feature modules — ADR #4). */
enum class HarmonyMode { TRADITIONAL, OKLCH }

enum class ColorHarmony {
    COMPLEMENTARY, ANALOGOUS, TRIADIC, SPLIT_COMPLEMENTARY, TETRADIC, MONOCHROMATIC;

    internal val hueOffsets: List<Float>
        get() = when (this) {
            COMPLEMENTARY -> listOf(0f, 180f)
            ANALOGOUS -> listOf(-30f, 0f, 30f)
            TRIADIC -> listOf(0f, 120f, 240f)
            SPLIT_COMPLEMENTARY -> listOf(0f, 150f, 210f)
            TETRADIC -> listOf(0f, 90f, 180f, 270f)
            MONOCHROMATIC -> listOf(0f)
        }

    fun generate(base: Hsv): List<Hsv> = generate(base, HarmonyMode.TRADITIONAL)

    fun generate(base: Hsv, mode: HarmonyMode): List<Hsv> = when (this) {
        MONOCHROMATIC -> {
            val hsl = base.toHsl()
            listOf(0.92f, 0.75f, 0.55f, 0.35f, 0.18f)
                .map { l -> hsl.copy(lightness = l).toRgb().toHsv() }
        }
        else -> when (mode) {
            HarmonyMode.TRADITIONAL ->
                hueOffsets.map { base.copy(hue = ColorConverter.normalizeHue(base.hue + it)) }
            HarmonyMode.OKLCH -> {
                val lch = base.toRgb().toOkLch()
                hueOffsets.map { offset ->
                    OkLch(
                        lightness = lch.lightness,
                        chroma = lch.chroma,
                        hueDegrees = ColorConverter.normalizeHue((lch.hueDegrees + offset).toFloat()).toDouble(),
                    ).toRgbClamped().toHsv()
                }
            }
        }
    }
}
