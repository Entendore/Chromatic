package com.example.chromatic.core.color

import kotlin.math.atan2
import kotlin.math.cbrt
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

/** Shared sRGB transfer functions. toLinear is tabulated — innermost op of every hot path. */
internal object SrgbTransfer {
    private val LINEAR = DoubleArray(256) { ch ->
        val s = ch / 255.0
        if (s <= 0.04045) s / 12.92 else ((s + 0.055) / 1.055).pow(2.4)
    }

    fun toLinear(channel: Int): Double = LINEAR[channel]

    fun fromLinear(linear: Double): Int {
        val s = if (linear <= 0.0031308) linear * 12.92 else 1.055 * linear.pow(1 / 2.4) - 0.055
        return (s * 255.0).roundToInt().coerceIn(0, 255)
    }
}

/**
 * OKLab (Björn Ottosson, 2020) — the perceptual space behind CSS Color 4.
 * Distances match human perception, unlike RGB or HSL.
 */
data class OkLab(val l: Double, val a: Double, val b: Double) {
    val chroma: Double get() = sqrt(a * a + b * b)
    val hueDegrees: Double get() = ((atan2(b, a) * 180.0 / kotlin.math.PI) + 360.0) % 360.0
}

object OkLabConverter {
    fun fromRgb(rgb: Rgb): OkLab {
        val r = SrgbTransfer.toLinear(rgb.red)
        val g = SrgbTransfer.toLinear(rgb.green)
        val b = SrgbTransfer.toLinear(rgb.blue)
        val l_ = cbrt(0.4122214708 * r + 0.5363325363 * g + 0.0514459929 * b)
        val m_ = cbrt(0.2119034982 * r + 0.6806995451 * g + 0.1073969566 * b)
        val s_ = cbrt(0.0883024619 * r + 0.2817188376 * g + 0.6299787005 * b)
        return OkLab(
            l = 0.2104542553 * l_ + 0.7936177850 * m_ - 0.0040720468 * s_,
            a = 1.9779984951 * l_ - 2.4285922050 * m_ + 0.4505937099 * s_,
            b = 0.0259040371 * l_ + 0.7827717662 * m_ - 0.8086757660 * s_,
        )
    }

    fun toRgb(lab: OkLab): Rgb {
        val (r, g, b) = lab.toLinearSrgb()
        return Rgb(SrgbTransfer.fromLinear(r), SrgbTransfer.fromLinear(g), SrgbTransfer.fromLinear(b))
    }
}

/** Linear-light sRGB before encoding — the basis for gamut checks. */
internal fun OkLab.toLinearSrgb(): DoubleArray {
    val l_ = l + 0.3963377774 * a + 0.2158037573 * b
    val m_ = l - 0.1055613458 * a - 0.0638541728 * b
    val s_ = l - 0.0894841775 * a - 1.2914855480 * b
    val l3 = l_ * l_ * l_; val m3 = m_ * m_ * m_; val s3 = s_ * s_ * s_
    return doubleArrayOf(
        4.0767416621 * l3 - 3.3077115913 * m3 + 0.2309699292 * s3,
        -1.2684380046 * l3 + 2.6097574011 * m3 - 0.3413193965 * s3,
        -0.0041960863 * l3 - 0.7034186147 * m3 + 1.7076147010 * s3,
    )
}

fun Rgb.toOkLab(): OkLab = OkLabConverter.fromRgb(this)
fun OkLab.toRgb(): Rgb = OkLabConverter.toRgb(this)

/** Perceptual difference: 0 = identical, ~0.02 just noticeable, >0.2 clearly different. */
fun deltaEOkLab(first: Rgb, second: Rgb): Double {
    val a = first.toOkLab(); val b = second.toOkLab()
    val dl = a.l - b.l; val da = a.a - b.a; val db = a.b - b.b
    return sqrt(dl * dl + da * da + db * db)
}
