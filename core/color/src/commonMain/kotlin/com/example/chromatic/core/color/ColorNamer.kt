package com.example.chromatic.core.color

/** Human-friendly nearest names, matched by perceptual (OKLab) distance. */
object ColorNamer {

    private val names: List<Pair<String, OkLab>> = listOf(
        "Black" to 0x000000, "Charcoal" to 0x36454F, "Dim Gray" to 0x696969, "Gray" to 0x808080,
        "Silver" to 0xC0C0C0, "White Smoke" to 0xF5F5F5, "White" to 0xFFFFFF,
        "Maroon" to 0x800000, "Brick Red" to 0xB22222, "Red" to 0xFF0000, "Crimson" to 0xDC143C,
        "Salmon" to 0xFA8072, "Coral" to 0xFF7F50, "Tomato" to 0xFF6347, "Orange Red" to 0xFF4500,
        "Dark Orange" to 0xFF8C00, "Orange" to 0xFFA500, "Gold" to 0xFFD700, "Yellow" to 0xFFFF00,
        "Khaki" to 0xF0E68C, "Beige" to 0xF5F5DC, "Wheat" to 0xF5DEB3, "Tan" to 0xD2B48C,
        "Goldenrod" to 0xDAA520, "Peru" to 0xCD853F, "Chocolate" to 0xD2691E, "Saddle Brown" to 0x8B4513,
        "Brown" to 0xA52A2A, "Sandy Brown" to 0xF4A460, "Olive" to 0x808000, "Olive Drab" to 0x6B8E23,
        "Yellow Green" to 0x9ACD32, "Lime" to 0x00FF00, "Lime Green" to 0x32CD32, "Lawn Green" to 0x7CFC00,
        "Forest Green" to 0x228B22, "Green" to 0x008000, "Sea Green" to 0x2E8B57, "Medium Sea Green" to 0x3CB371,
        "Spring Green" to 0x00FF7F, "Teal" to 0x008080, "Light Sea Green" to 0x20B2AA, "Turquoise" to 0x40E0D0,
        "Aqua" to 0x00FFFF, "Sky Blue" to 0x87CEEB, "Deep Sky Blue" to 0x00BFFF, "Dodger Blue" to 0x1E90FF,
        "Cornflower Blue" to 0x6495ED, "Steel Blue" to 0x4682B4, "Royal Blue" to 0x4169E1,
        "Blue" to 0x0000FF, "Medium Blue" to 0x0000CD, "Navy" to 0x000080, "Midnight Blue" to 0x191970,
        "Cadet Blue" to 0x5F9EA0, "Powder Blue" to 0xB0E0E6, "Slate Blue" to 0x6A5ACD, "Indigo" to 0x4B0082,
        "Purple" to 0x800080, "Rebecca Purple" to 0x663399, "Blue Violet" to 0x8A2BE2, "Dark Violet" to 0x9400D3,
        "Violet" to 0xEE82EE, "Orchid" to 0xDA70D6, "Magenta" to 0xFF00FF, "Plum" to 0xDDA0DD,
        "Thistle" to 0xD8BFD8, "Lavender" to 0xE6E6FA, "Pink" to 0xFFC0CB, "Hot Pink" to 0xFF69B4,
        "Deep Pink" to 0xFF1493, "Rosy Brown" to 0xBC8F8F, "Slate Gray" to 0x708090,
    ).map { (name, hex) ->
        name to Rgb((hex shr 16) and 0xFF, (hex shr 8) and 0xFF, hex and 0xFF).toOkLab()
    }

    fun nameOf(rgb: Rgb): String {
        val lab = rgb.toOkLab()
        return names.minBy { (_, candidate) ->
            val dl = lab.l - candidate.l; val da = lab.a - candidate.a; val db = lab.b - candidate.b
            dl * dl + da * da + db * db
        }.first
    }

    fun namePool(): List<String> = names.map { it.first }

    fun hueOf(name: String): Double? = names.firstOrNull { it.first == name }?.second?.hueDegrees
}
