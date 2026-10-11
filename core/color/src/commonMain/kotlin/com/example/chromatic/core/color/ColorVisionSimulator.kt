package com.example.chromatic.core.color

import kotlin.math.pow
import kotlin.math.roundToInt

enum class ColorVisionType {
    NORMAL, PROTANOPIA, DEUTERANOPIA, TRITANOPIA, ACHROMATOPSIA
}

object ColorVisionSimulator {

    // Machado, Oliveira & Fernandes (2009), severity 1.0 — LINEAR sRGB (reference model).
    private val PROTANOPIA = floatArrayOf(
        0.152286f, 1.052583f, -0.204868f,
        0.114503f, 0.786281f, 0.099216f,
        -0.003882f, -0.048116f, 1.051998f
    )
    private val DEUTERANOPIA = floatArrayOf(
        0.367322f, 0.860646f, -0.227968f,
        0.280085f, 0.672501f, 0.047413f,
        -0.011820f, 0.042940f, 0.968881f
    )
    private val TRITANOPIA = floatArrayOf(
        1.255528f, -0.076749f, -0.178779f,
        -0.078411f, 0.930809f, 0.147602f,
        0.004733f, 0.691367f, 0.303900f
    )

    fun simulate(rgb: Rgb, type: ColorVisionType): Rgb {
        if (type == ColorVisionType.NORMAL) return rgb

        val r = SrgbTransfer.toLinear(rgb.red)
        val g = SrgbTransfer.toLinear(rgb.green)
        val b = SrgbTransfer.toLinear(rgb.blue)

        if (type == ColorVisionType.ACHROMATOPSIA) {
            val y = 0.2126 * r + 0.7152 * g + 0.0722 * b
            val gray = SrgbTransfer.fromLinear(y)
            return Rgb(gray, gray, gray)
        }

        val m = when (type) {
            ColorVisionType.PROTANOPIA -> PROTANOPIA
            ColorVisionType.DEUTERANOPIA -> DEUTERANOPIA
            else -> TRITANOPIA
        }
        return Rgb(
            red = SrgbTransfer.fromLinear((m[0] * r + m[1] * g + m[2] * b).coerceIn(0.0, 1.0)),
            green = SrgbTransfer.fromLinear((m[3] * r + m[4] * g + m[5] * b).coerceIn(0.0, 1.0)),
            blue = SrgbTransfer.fromLinear((m[6] * r + m[7] * g + m[8] * b).coerceIn(0.0, 1.0)),
        )
    }

    /** In-place ARGB pixel buffer transform (pure — no android.graphics). */
    fun simulateArgb(pixels: IntArray, type: ColorVisionType): IntArray {
        for (i in pixels.indices) {
            val p = pixels[i]
            val a = (p ushr 24) and 0xFF
            val simulated = simulate(Rgb((p ushr 16) and 0xFF, (p ushr 8) and 0xFF, p and 0xFF), type)
            pixels[i] = (a shl 24) or (simulated.red shl 16) or (simulated.green shl 8) or simulated.blue
        }
        return pixels
    }

    /**
     * 4×5 row-major framework ColorMatrix applied to gamma-encoded sRGB — for
     * real-time whole-app previews (approximate; [simulate] is the reference).
     */
    fun srgbMatrix(type: ColorVisionType): FloatArray? = when (type) {
        ColorVisionType.NORMAL -> null
        ColorVisionType.PROTANOPIA -> floatArrayOf(
            0.567f, 0.433f, 0f, 0f, 0f,
            0.558f, 0.442f, 0f, 0f, 0f,
            0f, 0.242f, 0.758f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f,
        )
        ColorVisionType.DEUTERANOPIA -> floatArrayOf(
            0.625f, 0.375f, 0f, 0f, 0f,
            0.700f, 0.300f, 0f, 0f, 0f,
            0f, 0.300f, 0.700f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f,
        )
        ColorVisionType.TRITANOPIA -> floatArrayOf(
            0.950f, 0.050f, 0f, 0f, 0f,
            0f, 0.433f, 0.567f, 0f, 0f,
            0f, 0.475f, 0.525f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f,
        )
        ColorVisionType.ACHROMATOPSIA -> floatArrayOf(
            0.2126f, 0.7152f, 0.0722f, 0f, 0f,
            0.2126f, 0.7152f, 0.0722f, 0f, 0f,
            0.2126f, 0.7152f, 0.0722f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f,
        )
    }
}
