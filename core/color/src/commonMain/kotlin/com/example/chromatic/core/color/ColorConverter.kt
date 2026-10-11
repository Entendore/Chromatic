package com.example.chromatic.core.color

import kotlin.math.abs
import kotlin.math.roundToInt

/** Hue in [0, 360), saturation/lightness/value in [0, 1]. */
data class Hsv(val hue: Float, val saturation: Float, val value: Float)
data class Hsl(val hue: Float, val saturation: Float, val lightness: Float)

object ColorConverter {

    fun normalizeHue(degrees: Float): Float = ((degrees % 360f) + 360f) % 360f

    fun rgbToHsv(rgb: Rgb): Hsv {
        val r = rgb.red / 255f; val g = rgb.green / 255f; val b = rgb.blue / 255f
        val max = maxOf(r, g, b); val min = minOf(r, g, b); val delta = max - min
        val hue = when {
            delta == 0f -> 0f
            max == r -> 60f * (((g - b) / delta) % 6f)
            max == g -> 60f * ((b - r) / delta + 2f)
            else -> 60f * ((r - g) / delta + 4f)
        }
        return Hsv(normalizeHue(hue), if (max == 0f) 0f else delta / max, max)
    }

    fun hsvToRgb(hsv: Hsv): Rgb {
        val h = normalizeHue(hsv.hue)
        val c = hsv.value * hsv.saturation
        val x = c * (1f - abs(h / 60f % 2f - 1f))
        val (r1, g1, b1) = when ((h / 60f).toInt()) {
            0 -> Triple(c, x, 0f); 1 -> Triple(x, c, 0f); 2 -> Triple(0f, c, x)
            3 -> Triple(0f, x, c); 4 -> Triple(x, 0f, c); else -> Triple(c, 0f, x)
        }
        val m = hsv.value - c
        fun scale(v: Float) = ((v + m) * 255f).roundToInt().coerceIn(0, 255)
        return Rgb(scale(r1), scale(g1), scale(b1))
    }

    fun rgbToHsl(rgb: Rgb): Hsl {
        val r = rgb.red / 255f; val g = rgb.green / 255f; val b = rgb.blue / 255f
        val max = maxOf(r, g, b); val min = minOf(r, g, b)
        val lightness = (max + min) / 2f
        val delta = max - min
        val saturation = when {
            delta == 0f -> 0f
            lightness > 0.5f -> delta / (2f - max - min)
            else -> delta / (max + min)
        }
        val hue = when {
            delta == 0f -> 0f
            max == r -> 60f * (((g - b) / delta) % 6f)
            max == g -> 60f * ((b - r) / delta + 2f)
            else -> 60f * ((r - g) / delta + 4f)
        }
        return Hsl(normalizeHue(hue), saturation, lightness)
    }

    fun hslToRgb(hsl: Hsl): Rgb {
        val h = normalizeHue(hsl.hue)
        val c = (1f - abs(2f * hsl.lightness - 1f)) * hsl.saturation
        val x = c * (1f - abs(h / 60f % 2f - 1f))
        val (r1, g1, b1) = when ((h / 60f).toInt()) {
            0 -> Triple(c, x, 0f); 1 -> Triple(x, c, 0f); 2 -> Triple(0f, c, x)
            3 -> Triple(0f, x, c); 4 -> Triple(x, 0f, c); else -> Triple(c, 0f, x)
        }
        val m = hsl.lightness - c / 2f
        fun scale(v: Float) = ((v + m) * 255f).roundToInt().coerceIn(0, 255)
        return Rgb(scale(r1), scale(g1), scale(b1))
    }
}

fun Rgb.toHsv(): Hsv = ColorConverter.rgbToHsv(this)
fun Rgb.toHsl(): Hsl = ColorConverter.rgbToHsl(this)
fun Hsv.toRgb(): Rgb = ColorConverter.hsvToRgb(this)
fun Hsl.toRgb(): Rgb = ColorConverter.hslToRgb(this)
fun Hsv.toHsl(): Hsl = toRgb().toHsl()
fun Hsl.toHsv(): Hsv = toRgb().toHsv()
