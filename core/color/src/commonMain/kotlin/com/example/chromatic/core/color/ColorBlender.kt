package com.example.chromatic.core.color

import kotlin.math.roundToInt

enum class BlendSpace { SRGB, LINEAR_SRGB, OKLAB }

object ColorBlender {

    fun blend(from: Rgb, to: Rgb, t: Float, space: BlendSpace = BlendSpace.OKLAB): Rgb {
        val f = t.coerceIn(0f, 1f).toDouble()
        return when (space) {
            BlendSpace.SRGB -> Rgb(
                red = lerpChannel(from.red, to.red, f),
                green = lerpChannel(from.green, to.green, f),
                blue = lerpChannel(from.blue, to.blue, f),
            )
            BlendSpace.LINEAR_SRGB -> Rgb(
                red = SrgbTransfer.fromLinear(lerp(SrgbTransfer.toLinear(from.red), SrgbTransfer.toLinear(to.red), f)),
                green = SrgbTransfer.fromLinear(lerp(SrgbTransfer.toLinear(from.green), SrgbTransfer.toLinear(to.green), f)),
                blue = SrgbTransfer.fromLinear(lerp(SrgbTransfer.toLinear(from.blue), SrgbTransfer.toLinear(to.blue), f)),
            )
            BlendSpace.OKLAB -> {
                val a = from.toOkLab(); val b = to.toOkLab()
                OkLab(l = lerp(a.l, b.l, f), a = lerp(a.a, b.a, f), b = lerp(a.b, b.b, f)).toRgb()
            }
        }
    }

    fun ramp(from: Rgb, to: Rgb, stops: Int, space: BlendSpace = BlendSpace.OKLAB): List<Rgb> {
        require(stops >= 2) { "A ramp needs at least 2 stops" }
        return List(stops) { i -> blend(from, to, i / (stops - 1).toFloat(), space) }
    }

    private fun lerp(x: Double, y: Double, t: Double) = x + (y - x) * t
    private fun lerpChannel(x: Int, y: Int, t: Double) = (x + (y - x) * t).roundToInt().coerceIn(0, 255)
}
