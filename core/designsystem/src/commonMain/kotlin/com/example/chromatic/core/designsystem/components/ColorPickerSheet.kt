package com.example.chromatic.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import chromatic.designsystem.res.Res
import chromatic.designsystem.res.axis_chroma_oklch
import chromatic.designsystem.res.axis_hue
import chromatic.designsystem.res.axis_hue_oklch
import chromatic.designsystem.res.axis_lightness
import chromatic.designsystem.res.axis_lightness_oklch
import chromatic.designsystem.res.axis_saturation
import chromatic.designsystem.res.picker_cancel
import chromatic.designsystem.res.picker_gamut_note
import chromatic.designsystem.res.picker_tab_hsl
import chromatic.designsystem.res.picker_tab_oklch
import chromatic.designsystem.res.picker_use_color
import com.example.chromatic.core.color.ColorNamer
import com.example.chromatic.core.color.GamutMapper
import com.example.chromatic.core.color.Hsl
import com.example.chromatic.core.color.OkLch
import com.example.chromatic.core.color.Rgb
import com.example.chromatic.core.color.toHsl
import com.example.chromatic.core.color.toOkLch
import com.example.chromatic.core.color.toRgb
import com.example.chromatic.core.color.toRgbClamped
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

private fun threeDecimals(v: Double): String {
    val scaled = (v * 1000).roundToInt()
    return "${scaled / 1000}.${(scaled % 1000).toString().padStart(3, '0')}"
}

/** Touch color picking. The OKLCH chroma slider is bounded by the sRGB gamut —
 *  every producible color is displayable (v6). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorPickerSheet(
    initial: Rgb,
    onDismiss: () -> Unit,
    onSelected: (Rgb) -> Unit,
) {
    var rgb by remember { mutableStateOf(initial) }
    var tab by rememberSaveable { mutableStateOf(0) }
    val hsl = rgb.toHsl()
    val lch = rgb.toOkLch()
    val maxChroma = remember(lch.lightness, lch.hueDegrees) {
        GamutMapper.maxChroma(lch.lightness, lch.hueDegrees)
    }
    val hueGradient = listOf(
        Color(0xFFFF0000), Color(0xFFFFFF00), Color(0xFF00FF00),
        Color(0xFF00FFFF), Color(0xFF0000FF), Color(0xFFFF00FF), Color(0xFFFF0000),
    )

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(Modifier.size(56.dp).clip(CircleShape).background(rgb.toComposeColor()))
                Column {
                    Text(rgb.hex, style = MaterialTheme.typography.titleMedium)
                    Text(
                        ColorNamer.nameOf(rgb),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 },
                    text = { Text(stringResource(Res.string.picker_tab_hsl)) })
                Tab(selected = tab == 1, onClick = { tab = 1 },
                    text = { Text(stringResource(Res.string.picker_tab_oklch)) })
            }

            if (tab == 0) {
                LabeledSlider(
                    stringResource(Res.string.axis_hue), "${hsl.hue.toInt()}°", hsl.hue,
                    { rgb = hsl.copy(hue = it).toRgb() }, 0f..360f, hueGradient,
                )
                LabeledSlider(
                    stringResource(Res.string.axis_saturation), "${(hsl.saturation * 100).toInt()}%", hsl.saturation,
                    { rgb = hsl.copy(saturation = it).toRgb() }, 0f..1f,
                    listOf(Color.Gray, Color.hsl(hsl.hue, 1f, 0.5f)),
                )
                LabeledSlider(
                    stringResource(Res.string.axis_lightness), "${(hsl.lightness * 100).toInt()}%", hsl.lightness,
                    { rgb = hsl.copy(lightness = it).toRgb() }, 0f..1f,
                    listOf(Color.Black, Color.hsl(hsl.hue, hsl.saturation, 0.5f), Color.White),
                )
            } else {
                LabeledSlider(
                    stringResource(Res.string.axis_lightness_oklch), "${(lch.lightness * 100).toInt()}%", lch.lightness.toFloat(),
                    { rgb = OkLch(it.toDouble(), lch.chroma, lch.hueDegrees).toRgbClamped() }, 0f..1f,
                    listOf(Color.Black, Color.hsl(lch.hueDegrees.toFloat(), 0.7f, 0.5f), Color.White),
                )
                LabeledSlider(
                    stringResource(Res.string.axis_chroma_oklch), threeDecimals(lch.chroma), lch.chroma.toFloat(),
                    { rgb = OkLch(lch.lightness, it.toDouble(), lch.hueDegrees).toRgbClamped() },
                    0f..maxChroma.toFloat().coerceAtLeast(0.01f),
                    listOf(Color(0xFF9E9E9E), Color.hsl(lch.hueDegrees.toFloat(), 1f, 0.5f)),
                )
                LabeledSlider(
                    stringResource(Res.string.axis_hue_oklch), "${lch.hueDegrees.toInt()}°", lch.hueDegrees.toFloat(),
                    { rgb = OkLch(lch.lightness, lch.chroma, it.toDouble()).toRgbClamped() }, 0f..360f, hueGradient,
                )
                Text(
                    stringResource(Res.string.picker_gamut_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                    Text(stringResource(Res.string.picker_cancel))
                }
                Button(onClick = { onSelected(rgb) }, modifier = Modifier.weight(1f)) {
                    Text(stringResource(Res.string.picker_use_color))
                }
            }
        }
    }
}
