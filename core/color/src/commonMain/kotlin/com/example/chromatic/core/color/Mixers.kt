package com.example.chromatic.core.color

import kotlin.math.pow

/** Additive mixing — what screens do. Light sums in linear space. */
object LightMixer {
    fun add(a: Rgb, b: Rgb): Rgb {
        fun ch(x: Int, y: Int) =
            SrgbTransfer.fromLinear((SrgbTransfer.toLinear(x) + SrgbTransfer.toLinear(y)).coerceAtMost(1.0))
        return Rgb(ch(a.red, b.red), ch(a.green, b.green), ch(a.blue, b.blue))
    }
}

/** Idealized subtractive mixing — transmissions multiply in linear space. */
object SubtractiveMixer {
    fun mix(from: Rgb, to: Rgb, t: Float): Rgb {
        val f = t.coerceIn(0f, 1f).toDouble()
        fun channel(a: Int, b: Int) =
            SrgbTransfer.toLinear(a).pow(1.0 - f) * SrgbTransfer.toLinear(b).pow(f)
        return Rgb(
            red = SrgbTransfer.fromLinear(channel(from.red, to.red)),
            green = SrgbTransfer.fromLinear(channel(from.green, to.green)),
            blue = SrgbTransfer.fromLinear(channel(from.blue, to.blue)),
        )
    }
}
