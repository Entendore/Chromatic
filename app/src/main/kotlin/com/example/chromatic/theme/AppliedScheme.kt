package com.example.chromatic.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import com.example.chromatic.core.color.GeneratedScheme
import com.example.chromatic.core.designsystem.components.toComposeColor

/**
 * ThemeGenerator guarantees exactly these 17 roles (tested in :core:color) —
 * getValue is total. Error roles are absent by design and fall back to M3 defaults.
 */
fun GeneratedScheme.toColorScheme(): ColorScheme {
    fun role(key: String) = roles.getValue(key).toComposeColor()
    return if (isDark) {
        darkColorScheme(
            primary = role("primary"), onPrimary = role("onPrimary"),
            primaryContainer = role("primaryContainer"), onPrimaryContainer = role("onPrimaryContainer"),
            secondary = role("secondary"), onSecondary = role("onSecondary"),
            secondaryContainer = role("secondaryContainer"), onSecondaryContainer = role("onSecondaryContainer"),
            tertiary = role("tertiary"), onTertiary = role("onTertiary"),
            tertiaryContainer = role("tertiaryContainer"), onTertiaryContainer = role("onTertiaryContainer"),
            surface = role("surface"), onSurface = role("onSurface"),
            surfaceVariant = role("surfaceVariant"), onSurfaceVariant = role("onSurfaceVariant"),
            outline = role("outline"),
        )
    } else {
        lightColorScheme(
            primary = role("primary"), onPrimary = role("onPrimary"),
            primaryContainer = role("primaryContainer"), onPrimaryContainer = role("onPrimaryContainer"),
            secondary = role("secondary"), onSecondary = role("onSecondary"),
            secondaryContainer = role("secondaryContainer"), onSecondaryContainer = role("onSecondaryContainer"),
            tertiary = role("tertiary"), onTertiary = role("onTertiary"),
            tertiaryContainer = role("tertiaryContainer"), onTertiaryContainer = role("onTertiaryContainer"),
            surface = role("surface"), onSurface = role("onSurface"),
            surfaceVariant = role("surfaceVariant"), onSurfaceVariant = role("onSurfaceVariant"),
            outline = role("outline"),
        )
    }
}
