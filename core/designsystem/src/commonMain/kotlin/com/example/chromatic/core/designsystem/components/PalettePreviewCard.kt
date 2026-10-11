package com.example.chromatic.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.chromatic.core.color.Rgb
import com.example.chromatic.core.color.toHsl
import com.example.chromatic.core.color.toRgb

/**
 * Demonstrates "applying" a palette: the dominant hue tints a container, content
 * colors are derived with the WCAG engine, the last color becomes the action color.
 */
@Composable
fun PalettePreviewCard(palette: List<Rgb>, modifier: Modifier = Modifier) {
    if (palette.isEmpty()) return
    val baseHsl = palette.first().toHsl()
    val container = baseHsl.copy(lightness = 0.94f).toRgb()
    val onContainer = baseHsl.copy(lightness = 0.25f).toRgb()
    val action = palette.last()
    val onAction = readableTextColor(action)

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = container.toComposeColor()),
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                "Sample app card",
                style = MaterialTheme.typography.titleMedium,
                color = onContainer.toComposeColor(),
            )
            Text(
                "Text colors are derived automatically to pass WCAG AA against their background.",
                style = MaterialTheme.typography.bodySmall,
                color = onContainer.toComposeColor().copy(alpha = 0.75f),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                palette.forEach { rgb ->
                    Box(Modifier.size(22.dp).clip(CircleShape).background(rgb.toComposeColor()))
                }
            }
            Button(
                onClick = {},
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = action.toComposeColor(),
                    contentColor = onAction,
                ),
            ) { Text("Continue") }
        }
    }
}
