package com.example.chromatic.core.designsystem.components

import androidx.compose.ui.graphics.Color
import com.example.chromatic.core.color.Rgb
import com.example.chromatic.core.color.WcagContrast

fun Rgb.toComposeColor(): Color = Color(red = red, green = green, blue = blue)

/** Uses the WCAG engine to guarantee readable text on any swatch. */
fun readableTextColor(background: Rgb): Color {
    val onWhite = WcagContrast.contrastRatio(Rgb.WHITE, background)
    val onBlack = WcagContrast.contrastRatio(Rgb.BLACK, background)
    return if (onWhite >= onBlack) Color.White else Color.Black
}
