package com.example.chromatic.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Application
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import com.example.chromatic.core.color.PaletteStyle
import com.example.chromatic.core.designsystem.components.CopyableSwatch
import com.example.chromatic.core.designsystem.components.LabeledSlider
import com.example.chromatic.core.designsystem.components.PassFailChip
import com.example.chromatic.core.designsystem.components.SectionTitle
import com.example.chromatic.core.designsystem.components.toComposeColor
import com.example.chromatic.core.designsystem.theme.ChromaticTheme
import androidx.compose.ui.window.exitApplication

fun main() {
    val restored = WindowStateStore.load()
    Application {
        val state = rememberWindowState(
            size = restored?.size ?: DpSize(820.dp, 640.dp),
            position = restored?.position ?: WindowPosition.PlatformWindowPosition,
        )
        Window(
            onCloseRequest = { WindowStateStore.save(state); exitApplication() },
            title = "Chromatic — Palette Studio",
            state = state,
        ) {
            ChromaticTheme(dynamicColor = false) { PaletteStudio() }
        }
    }
}

private fun styleLabel(s: PaletteStyle): String =
    s.name.lowercase().replaceFirstChar(Char::uppercaseChar)

@Composable
private fun PaletteStudio() {
    var hue by rememberSaveable { mutableFloatStateOf(220f) }
    var style by rememberSaveable { mutableStateOf(PaletteStyle.JEWEL) }
    var seed by rememberSaveable { mutableIntStateOf(0) }
    val model = remember(style, hue, seed) { PlaygroundModel(style, hue.toDouble(), seed) }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("Palette Studio (desktop)", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Theme, sliders, and swatches are :core:designsystem composables — the same code the phone renders.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        LabeledSlider(
            label = "Hue", valueText = "${hue.toInt()}°", value = hue,
            onValueChange = { hue = it }, valueRange = 0f..360f,
            previewGradient = listOf(
                Color(0xFFFF0000), Color(0xFFFFFF00), Color(0xFF00FF00),
                Color(0xFF00FFFF), Color(0xFF0000FF), Color(0xFFFF00FF), Color(0xFFFF0000),
            ),
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PaletteStyle.entries.forEach { s ->
                FilterChip(
                    selected = style == s,
                    onClick = { style = s; seed++ },
                    label = { Text(styleLabel(s)) },
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            model.colors.forEach { rgb ->
                CopyableSwatch(rgb = rgb, modifier = Modifier.weight(1f))
            }
        }

        SectionTitle("Tonal scale")
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
            model.tones.forEach { tone ->
                Box(
                    Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(tone.rgb.toComposeColor())
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { seed++ }) { Text("Regenerate") }
            PassFailChip(
                "${model.darkToneLevels.size} tones pass AA on white",
                model.darkToneLevels.isNotEmpty(),
            )
        }
        Spacer(Modifier.size(4.dp))
    }
}
