package com.example.chromatic.core.data.theme

enum class ThemeSource { APPLIED, DYNAMIC, STOCK }

fun resolveThemeSource(appliedSeedHex: String?, dynamicEnabled: Boolean): ThemeSource =
    when {
        appliedSeedHex != null -> ThemeSource.APPLIED   // explicit user intent wins
        dynamicEnabled -> ThemeSource.DYNAMIC
        else -> ThemeSource.STOCK
    }
