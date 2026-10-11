package com.example.chromatic.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable

private val AppTypography = Typography()

@Composable
expect fun dynamicColorScheme(darkTheme: Boolean): ColorScheme?

/** Precedence (v18): applied override > dynamic color > stock scheme. */
@Composable
fun ChromaticTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    overrideScheme: ColorScheme? = null,
    content: @Composable () -> Unit,
) {
    val fallback = if (darkTheme) DarkColors else LightColors
    val scheme = overrideScheme
        ?: if (dynamicColor) dynamicColorScheme(darkTheme) ?: fallback else fallback
    MaterialTheme(colorScheme = scheme, typography = AppTypography, content = content)
}
