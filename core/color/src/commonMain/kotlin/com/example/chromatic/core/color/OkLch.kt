package com.example.chromatic.core.color

import kotlin.math.cos
import kotlin.math.sin

/** Cylindrical OKLab — L in [0,1], C ≥ 0, H in degrees. */
data class OkLch(val lightness: Double, val chroma: Double, val hueDegrees: Double) {
    init { require(chroma >= 0.0) { "Chroma must be non-negative" } }
}

fun OkLab.toOkLch(): OkLch = OkLch(l, chroma, hueDegrees)
fun OkLch.toOkLab(): OkLab =
    if (chroma == 0.0) OkLab(lightness, 0.0, 0.0)
    else OkLab(
        lightness,
        chroma * cos(hueDegrees * kotlin.math.PI / 180.0),
        chroma * sin(hueDegrees * kotlin.math.PI / 180.0),
    )

fun Rgb.toOkLch(): OkLch = toOkLab().toOkLch()

/** In-gamut conversion: chroma shrinks, channels are never clipped — hue survives. */
fun OkLch.toRgbClamped(): Rgb = GamutMapper.mapToSRgb(this)

object GamutMapper {
    private const val EPS = 1e-4
    private const val MAX_SEARCH_CHROMA = 0.4
    private const val TOLERANCE = 2e-4
    private const val L_STEPS = 512
    private const val CACHE_LIMIT = 4096

    // v10 lesson: cache stores search STATE (a bracket seed), never quantized RESULTS.
    private val seedCache = HashMap<Long, Double>()

    fun inGamut(lab: OkLab): Boolean = lab.toLinearSrgb().all { it in -EPS..1.0 + EPS }

    /** Fused Lch probe — same math as OkLch→OkLab→linear, minus allocations. */
    private fun inGamutLch(l: Double, c: Double, hDegrees: Double): Boolean {
        val rad = hDegrees * kotlin.math.PI / 180.0
        val a = c * cos(rad); val b = c * sin(rad)
        val l_ = l + 0.3963377774 * a + 0.2158037573 * b
        val m_ = l - 0.1055613458 * a - 0.0638541728 * b
        val s_ = l - 0.0894841775 * a - 1.2914855480 * b
        val l3 = l_ * l_ * l_; val m3 = m_ * m_ * m_; val s3 = s_ * s_ * s_
        val r = 4.0767416621 * l3 - 3.3077115913 * m3 + 0.2309699292 * s3
        val g = -1.2684380046 * l3 + 2.6097574011 * m3 - 0.3413193965 * s3
        val bl = -0.0041960863 * l3 - 0.7034186147 * m3 + 1.7076147010 * s3
        return r in -EPS..1.0 + EPS && g in -EPS..1.0 + EPS && bl in -EPS..1.0 + EPS
    }

    /** Largest chroma inside sRGB at (L, H) — bisection with early exit. */
    fun maxChroma(lightness: Double, hueDegrees: Double): Double {
        if (inGamutLch(lightness, MAX_SEARCH_CHROMA, hueDegrees)) return MAX_SEARCH_CHROMA
        var low = 0.0; var high = MAX_SEARCH_CHROMA
        while (high - low > TOLERANCE) {
            val mid = (low + high) / 2.0
            if (inGamutLch(lightness, mid, hueDegrees)) low = mid else high = mid
        }
        return low
    }

    /** Exact result, warm-started from the quantized cache. ~3–5 probes on hits. */
    fun maxChromaCached(lightness: Double, hueDegrees: Double): Double {
        if (inGamutLch(lightness, MAX_SEARCH_CHROMA, hueDegrees)) return MAX_SEARCH_CHROMA
        val lq = (lightness.coerceIn(0.0, 1.0) * (L_STEPS - 1)).toInt().toLong()
        val hq = ((hueDegrees % 360.0 + 360.0) % 360.0).toInt().toLong()
        val key = lq * 360L + hq
        val seed = synchronized(seedCache) { seedCache[key] }

        var low = 0.0; var high = MAX_SEARCH_CHROMA
        if (seed != null && seed > 0.0) {
            var step = 16 * TOLERANCE
            if (inGamutLch(lightness, seed, hueDegrees)) {
                low = seed; high = (seed + step).coerceAtMost(MAX_SEARCH_CHROMA)
                while (high < MAX_SEARCH_CHROMA && inGamutLch(lightness, high, hueDegrees)) {
                    low = high; step *= 2; high = (seed + step).coerceAtMost(MAX_SEARCH_CHROMA)
                }
            } else {
                high = seed; low = (seed - step).coerceAtLeast(0.0)
                while (low > 0.0 && !inGamutLch(lightness, low, hueDegrees)) {
                    high = low; step *= 2; low = (seed - step).coerceAtLeast(0.0)
                }
            }
        }
        while (high - low > TOLERANCE) {
            val mid = (low + high) / 2.0
            if (inGamutLch(lightness, mid, hueDegrees)) low = mid else high = mid
        }
        synchronized(seedCache) {
            if (seedCache.size > CACHE_LIMIT) seedCache.clear()
            seedCache[key] = low
        }
        return low
    }

    fun mapToSRgb(color: OkLch): Rgb {
        if (inGamut(color.toOkLab())) return color.toRgb()
        return color.copy(chroma = maxChroma(color.lightness, color.hueDegrees)).toRgb()
    }
}
