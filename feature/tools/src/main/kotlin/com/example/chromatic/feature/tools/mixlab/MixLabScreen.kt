package com.example.chromatic.feature.tools.mixlab

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.chromatic.core.color.Hsl
import com.example.chromatic.core.color.LightMixer
import com.example.chromatic.core.color.Rgb
import com.example.chromatic.core.color.SubtractiveMixer
import com.example.chromatic.core.color.toRgb
import com.example.chromatic.core.designsystem.components.LabeledSlider
import com.example.chromatic.core.designsystem.components.SectionTitle
import com.example.chromatic.core.designsystem.components.toComposeColor

private data class MixState(val a: Hsl, val b: Hsl) {
    val additive: Rgb get() = LightMixer.add(a.toRgb(), b.toRgb())
    val subtractive: Rgb get() = SubtractiveMixer.mix(a.toRgb(), b.toRgb(), 0.5f)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MixLabScreen() {
    var a by rememberSaveable(stateSaver = hslSaver()) { mutableStateOf(Hsl(0f, 0.95f, 0.5f)) }
    var b by rememberSaveable(stateSaver = hslSaver()) { mutableStateOf(Hsl(120f, 0.95f, 0.5f)) }
    val mix = MixState(a, b)

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Text(
            stringResource(R.string.mix_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(vertical = 16.dp),
        )
        Text(
            stringResource(R.string.mix_intro),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        SectionTitle(stringResource(R.string.mix_presets))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(
                stringResource(R.string.preset_red_green) to MixState(Hsl(0f, 0.95f, 0.5f), Hsl(120f, 0.95f, 0.5f)),
                stringResource(R.string.preset_blue_yellow) to MixState(Hsl(220f, 0.95f, 0.5f), Hsl(55f, 0.95f, 0.5f)),
                stringResource(R.string.preset_red_cyan) to MixState(Hsl(0f, 0.95f, 0.5f), Hsl(180f, 0.95f, 0.5f)),
            ).forEach { (label, preset) ->
                FilterChip(selected = false, onClick = { a = preset.a; b = preset.b }, label = { Text(label) })
            }
        }

        SectionTitle(stringResource(R.string.ingredient_a))
        IngredientSliders(a) { a = it }
        SectionTitle(stringResource(R.string.ingredient_b))
        IngredientSliders(b) { b = it }

        SectionTitle(stringResource(R.string.results_equal))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            MixPanel(stringResource(R.string.light_added), stringResource(R.string.screens_lights), mix.additive, Modifier.weight(1f))
            MixPanel(stringResource(R.string.pigment_mixed), stringResource(R.string.paint_ink), mix.subtractive, Modifier.weight(1f))
        }

        SectionTitle(stringResource(R.string.mix_notice))
        Text(
            stringResource(R.string.mix_notice_body),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(32.dp))
    }
}

private fun hslSaver() = androidx.compose.runtime.saveable.Saver<Hsl, List<Float>>(
    save = { listOf(it.hue, it.saturation, it.lightness) },
    restore = { Hsl(it[0], it[1], it[2]) },
)

@Composable
private fun IngredientSliders(hsl: Hsl, onChange: (Hsl) -> Unit) {
    val color = hsl.toRgb().toComposeColor()
    LabeledSlider(
        stringResource(R.string.axis_hue), "${hsl.hue.toInt()}°", hsl.hue,
        { onChange(hsl.copy(hue = it)) }, 0f..360f,
        listOf(
            Color(0xFFFF0000), Color(0xFFFFFF00), Color(0xFF00FF00),
            Color(0xFF00FFFF), Color(0xFF0000FF), Color(0xFFFF00FF), Color(0xFFFF0000),
        ),
    )
    LabeledSlider(
        stringResource(R.string.axis_saturation), "${(hsl.saturation * 100).toInt()}%", hsl.saturation,
        { onChange(hsl.copy(saturation = it)) }, 0f..1f,
        listOf(Color.Gray, Color.hsl(hsl.hue, 1f, 0.5f)),
    )
    LabeledSlider(
        stringResource(R.string.axis_lightness), "${(hsl.lightness * 100).toInt()}%", hsl.lightness,
        { onChange(hsl.copy(lightness = it)) }, 0f..1f,
        listOf(Color.Black, color, Color.White),
    )
}

@Composable
private fun MixPanel(title: String, subtitle: String, result: Rgb, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(result.toComposeColor())
            )
            Text(result.hex, style = MaterialTheme.typography.labelMedium)
        }
    }
}
