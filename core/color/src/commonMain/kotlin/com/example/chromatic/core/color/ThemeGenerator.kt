package com.example.chromatic.core.color

data class SchemeVerification(val label: String, val ratio: Double, val passes: Boolean)
data class GeneratedScheme(val isDark: Boolean, val roles: Map<String, Rgb>, val verifications: List<SchemeVerification>)
data class ThemeBundle(val light: GeneratedScheme, val dark: GeneratedScheme)

object ThemeGenerator {

    private val LIGHT_TONES = linkedMapOf(
        "primary" to 0.40, "onPrimary" to 0.99, "primaryContainer" to 0.90, "onPrimaryContainer" to 0.15,
        "secondary" to 0.40, "onSecondary" to 0.99, "secondaryContainer" to 0.90, "onSecondaryContainer" to 0.15,
        "tertiary" to 0.40, "onTertiary" to 0.99, "tertiaryContainer" to 0.90, "onTertiaryContainer" to 0.15,
        "surface" to 0.985, "onSurface" to 0.12, "surfaceVariant" to 0.90, "onSurfaceVariant" to 0.32,
        "outline" to 0.55,
    )
    private val DARK_TONES = linkedMapOf(
        "primary" to 0.85, "onPrimary" to 0.24, "primaryContainer" to 0.33, "onPrimaryContainer" to 0.92,
        "secondary" to 0.82, "onSecondary" to 0.24, "secondaryContainer" to 0.35, "onSecondaryContainer" to 0.92,
        "tertiary" to 0.85, "onTertiary" to 0.24, "tertiaryContainer" to 0.35, "onTertiaryContainer" to 0.92,
        "surface" to 0.08, "onSurface" to 0.92, "surfaceVariant" to 0.32, "onSurfaceVariant" to 0.82,
        "outline" to 0.60,
    )

    private val VERIFIED_PAIRS = listOf(
        "onPrimary" to "primary", "onPrimaryContainer" to "primaryContainer",
        "onSecondary" to "secondary", "onSecondaryContainer" to "secondaryContainer",
        "onTertiary" to "tertiary", "onTertiaryContainer" to "tertiaryContainer",
        "onSurface" to "surface", "onSurfaceVariant" to "surfaceVariant",
    )

    fun generate(seed: Rgb): ThemeBundle =
        ThemeBundle(light = buildScheme(seed, isDark = false), dark = buildScheme(seed, isDark = true))

    private fun buildScheme(seed: Rgb, isDark: Boolean): GeneratedScheme {
        val seedLch = seed.toOkLch()
        val maxAtSeed = GamutMapper.maxChroma(seedLch.lightness, seedLch.hueDegrees)
        val relativeChroma =
            if (maxAtSeed < 1e-6 || seedLch.chroma < 0.01) 0.0
            else (seedLch.chroma / maxAtSeed).coerceIn(0.40, 1.0)

        val primaryHue = seedLch.hueDegrees
        val tertiaryHue = (primaryHue + 60.0) % 360.0
        val primaryC = GamutMapper.maxChroma(0.5, primaryHue) * relativeChroma
        val secondaryC = primaryC * 0.40
        val tertiaryC = GamutMapper.maxChroma(0.5, tertiaryHue) * relativeChroma * 0.9
        val neutralC = primaryC * 0.06
        val variantC = primaryC * 0.14

        val roles = (if (isDark) DARK_TONES else LIGHT_TONES).mapValues { (name, lightness) ->
            roleColor(name, lightness, primaryHue, tertiaryHue, primaryC, secondaryC, tertiaryC, neutralC, variantC)
        }.toMutableMap()

        val verifications = VERIFIED_PAIRS.map { (fgName, bgName) ->
            val bg = roles.getValue(bgName)
            var fg = roles.getValue(fgName)
            var ratio = WcagContrast.contrastRatio(fg, bg)
            if (ratio < WcagContrast.AA_NORMAL) {
                val fixed = WcagContrast.adjustedForeground(fg, bg, WcagContrast.AA_NORMAL)
                fg = fixed
                roles[fgName] = fixed
                ratio = WcagContrast.contrastRatio(fixed, bg)
            }
            SchemeVerification("$fgName on $bgName", ratio, ratio >= WcagContrast.AA_NORMAL)
        }

        return GeneratedScheme(isDark, roles, verifications)
    }

    private fun roleColor(
        name: String, lightness: Double,
        primaryHue: Double, tertiaryHue: Double,
        primaryC: Double, secondaryC: Double, tertiaryC: Double,
        neutralC: Double, variantC: Double,
    ): Rgb {
        val isOnRole = name.startsWith("on")
        val family = when {
            name in setOf("surfaceVariant", "onSurfaceVariant", "outline") -> 5
            name.contains("Primary") -> 1
            name.contains("Secondary") -> 2
            name.contains("Tertiary") -> 3
            else -> 4
        }
        val (hue, chroma) = when (family) {
            1 -> primaryHue to (if (isOnRole) primaryC * 0.3 else primaryC)
            2 -> primaryHue to (if (isOnRole) secondaryC * 0.3 else secondaryC)
            3 -> tertiaryHue to (if (isOnRole) tertiaryC * 0.3 else tertiaryC)
            5 -> primaryHue to variantC
            else -> primaryHue to neutralC
        }
        return GamutMapper.mapToSRgb(OkLch(lightness, chroma, hue))
    }

    /** Ready-to-paste theme code for the user's own Compose project. */
    fun toComposeCode(bundle: ThemeBundle, name: String): String = buildString {
        listOf("Light" to bundle.light, "Dark" to bundle.dark).forEach { (label, scheme) ->
            appendLine("private val ${name}${label}Colors = ${if (scheme.isDark) "darkColorScheme" else "lightColorScheme"}(")
            scheme.roles.forEach { (role, rgb) ->
                appendLine("    $role = Color(0x${rgb.hex.removePrefix("#")}FF),")
            }
            appendLine(")")
            appendLine()
        }
    }.trimEnd()
}
