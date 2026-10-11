package com.example.chromatic.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val LightColors = lightColorScheme(
    primary = Color(0xFF4B5AC5), onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDEE1FF), onPrimaryContainer = Color(0xFF00105C),
    secondary = Color(0xFF00696E), onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF9CF1F6), onSecondaryContainer = Color(0xFF002022),
    tertiary = Color(0xFF7B4FA5), onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF3DAFF), onTertiaryContainer = Color(0xFF521A76),
    error = Color(0xFFBA1A1A), onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6), onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFBF8FF), onBackground = Color(0xFF1B1B21),
    surface = Color(0xFFFBF8FF), onSurface = Color(0xFF1B1B21),
    surfaceVariant = Color(0xFFE3E1EC), onSurfaceVariant = Color(0xFF46464F),
    outline = Color(0xFF777680),
)

val DarkColors = darkColorScheme(
    primary = Color(0xFFBAC3FF), onPrimary = Color(0xFF08218A),
    primaryContainer = Color(0xFF2C3FA0), onPrimaryContainer = Color(0xFFDEE1FF),
    secondary = Color(0xFF80D4DA), onSecondary = Color(0xFF003739),
    secondaryContainer = Color(0xFF004F53), onSecondaryContainer = Color(0xFF9CF1F6),
    tertiary = Color(0xFFE1B7FF), onTertiary = Color(0xFF4B2575),
    tertiaryContainer = Color(0xFF63378D), onTertiaryContainer = Color(0xFFF3DAFF),
    error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A), onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF131318), onBackground = Color(0xFFE4E1E9),
    surface = Color(0xFF131318), onSurface = Color(0xFFE4E1E9),
    surfaceVariant = Color(0xFF46464F), onSurfaceVariant = Color(0xFFC7C5D0),
    outline = Color(0xFF91909A),
)
