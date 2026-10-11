package com.example.chromatic.core.color

/** Platform-agnostic sRGB color. The domain layer never touches a UI framework. */
data class Rgb(val red: Int, val green: Int, val blue: Int) {

    init {
        require(red in 0..255 && green in 0..255 && blue in 0..255) {
            "RGB components must be in [0, 255] but was ($red, $green, $blue)"
        }
    }

    val hex: String
        get() = buildString(7) {
            append('#')
            fun byte(value: Int) {
                append(HEX[(value shr 4) and 0xF]); append(HEX[value and 0xF])
            }
            byte(red); byte(green); byte(blue)
        }

    fun toArgbInt(): Int = (0xFF shl 24) or (red shl 16) or (green shl 8) or blue

    companion object {
        val WHITE = Rgb(255, 255, 255)
        val BLACK = Rgb(0, 0, 0)
        private const val HEX = "0123456789ABCDEF"

        private fun hexDigit(c: Char): Int = when (c) {
            in '0'..'9' -> c - '0'
            in 'a'..'f' -> c - 'a' + 10
            in 'A'..'F' -> c - 'A' + 10
            else -> -1
        }

        /** Accepts "#FF5722", "FF5722", and shorthand "#F52". */
        fun fromHex(value: String): Rgb {
            val cleaned = value.trim().removePrefix("#")
            require(cleaned.isNotEmpty()) { "Empty hex color" }
            return when (cleaned.length) {
                3 -> {
                    val digits = cleaned.map { hexDigit(it) }
                    require(digits.all { it >= 0 }) { "Invalid hex color: \"$value\"" }
                    fun expand(d: Int) = d * 16 + d
                    Rgb(expand(digits[0]), expand(digits[1]), expand(digits[2]))
                }
                6 -> {
                    val digits = cleaned.map { hexDigit(it) }
                    require(digits.all { it >= 0 }) { "Invalid hex color: \"$value\"" }
                    Rgb(
                        digits[0] * 16 + digits[1],
                        digits[2] * 16 + digits[3],
                        digits[4] * 16 + digits[5],
                    )
                }
                else -> throw IllegalArgumentException("Unsupported hex format: \"$value\" (use #RGB or #RRGGBB)")
            }
        }

        fun fromHexOrNull(value: String): Rgb? = runCatching { fromHex(value) }.getOrNull()
    }
}
