package com.example.chromatic.feature.tools.illusionlab

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.chromatic.core.color.Hsl
import com.example.chromatic.core.color.Rgb
import com.example.chromatic.core.color.toRgb
import com.example.chromatic.core.designsystem.components.LabeledSlider
import com.example.chromatic.core.designsystem.components.SectionTitle
import com.example.chromatic.core.designsystem.components.toComposeColor

/** Pure UI state (v6 §7): no ViewModel — the illusion is its own lesson. */
@Composable
fun IllusionLabScreen() {
    var hue by rememberSaveable { mutableFloatStateOf(210f) }
    var saturation by rememberSaveable { mutableFloatStateOf(0.65f) }
    var revealed by rememberSaveable { mutableStateOf(false) }

    val tint = Hsl(hue, saturation, 0.93f).toRgb()
    val shade = Hsl(hue, saturation, 0.16f).toRgb()
    val center = Rgb(128, 128, 128)

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Text(
            stringResource(R.string.illusion_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(vertical = 16.dp),
        )
        Text(
            stringResource(R.string.illusion_intro),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            SurroundPanel(stringResource(R.string.on_light_surround), tint, center, Modifier.weight(1f))
            SurroundPanel(stringResource(R.string.on_dark_surround), shade, center, Modifier.weight(1f))
        }

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 12.dp)) {
            Switch(checked = revealed, onCheckedChange = { revealed = it })
            Spacer(Modifier.size(8.dp))
            Text(stringResource(R.string.reveal), style = MaterialTheme.typography.bodyMedium)
        }

        if (revealed) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) {
                Box(Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(center.toComposeColor()))
                Text("=", style = MaterialTheme.typography.titleLarge)
                Box(Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(center.toComposeColor()))
                Text(center.hex, style = MaterialTheme.typography.titleMedium)
            }
            Text(
                stringResource(R.string.illusion_revealed),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        LabeledSlider(
            label = stringResource(R.string.surround_hue),
            valueText = "${hue.toInt()}°",
            value = hue, onValueChange = { hue = it }, valueRange = 0f..360f,
            previewGradient = listOf(
                Color(0xFFFF0000), Color(0xFFFFFF00), Color(0xFF00FF00),
                Color(0xFF00FFFF), Color(0xFF0000FF), Color(0xFFFF00FF), Color(0xFFFF0000),
            ),
        )
        LabeledSlider(
            label = stringResource(R.string.surround_saturation),
            valueText = "${(saturation * 100).toInt()}%",
            value = saturation, onValueChange = { saturation = it }, valueRange = 0f..1f,
            previewGradient = listOf(Color.Gray, Color.hsl(hue, 1f, 0.5f)),
        )

        SectionTitle(stringResource(R.string.illusion_why))
        Text(
            stringResource(R.string.illusion_why_body),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun SurroundPanel(label: String, surround: Rgb, center: Rgb, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Box(
            Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .height(120.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(surround.toComposeColor()),
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)).background(center.toComposeColor()))
        }
    }
}
