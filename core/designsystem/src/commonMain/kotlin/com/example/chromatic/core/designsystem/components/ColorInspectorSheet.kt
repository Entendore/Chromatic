package com.example.chromatic.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import chromatic.designsystem.res.Res
import chromatic.designsystem.res.inspector_on_black
import chromatic.designsystem.res.inspector_on_white
import chromatic.designsystem.res.inspector_under_cvd
import com.example.chromatic.core.color.ColorNamer
import com.example.chromatic.core.color.ColorVisionSimulator
import com.example.chromatic.core.color.ColorVisionType
import com.example.chromatic.core.color.Rgb
import com.example.chromatic.core.color.WcagContrast
import com.example.chromatic.core.color.toHsl
import com.example.chromatic.core.color.toOkLch
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

/** Long-press any swatch for the full color breakdown (v3/v16). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorInspectorSheet(rgb: Rgb, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(rgb.toComposeColor())
            )
            Text(ColorNamer.nameOf(rgb), style = MaterialTheme.typography.headlineSmall)
            Text(rgb.hex, style = MaterialTheme.typography.titleMedium)

            InspectorRow("RGB", "${rgb.red}, ${rgb.green}, ${rgb.blue}")
            rgb.toHsl().let {
                InspectorRow(
                    "HSL",
                    "${it.hue.toInt()}°, ${(it.saturation * 100).toInt()}%, ${(it.lightness * 100).toInt()}%",
                )
            }
            rgb.toOkLch().let {
                InspectorRow(
                    "OKLCH",
                    "${(it.lightness * 100).toInt()}%, ${threeDecimals(it.chroma)}, ${it.hueDegrees.toInt()}°",
                )
            }
            InspectorRow(
                "Luminance",
                threeDecimals(WcagContrast.relativeLuminance(rgb)),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val onWhite = WcagContrast.contrastRatio(rgb, Rgb.WHITE)
                val onBlack = WcagContrast.contrastRatio(rgb, Rgb.BLACK)
                PassFailChip(
                    stringResource(Res.string.inspector_on_white, twoDecimals(onWhite)),
                    onWhite >= WcagContrast.AA_NORMAL,
                )
                PassFailChip(
                    stringResource(Res.string.inspector_on_black, twoDecimals(onBlack)),
                    onBlack >= WcagContrast.AA_NORMAL,
                )
            }

            SectionTitle(stringResource(Res.string.inspector_under_cvd))
            ColorVisionType.entries.filter { it != ColorVisionType.NORMAL }.forEach { type ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(Modifier.size(24.dp).clip(CircleShape).background(rgb.toComposeColor()))
                    Text("→", style = MaterialTheme.typography.labelMedium)
                    Box(
                        Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(ColorVisionSimulator.simulate(rgb, type).toComposeColor())
                    )
                    Text(type.label(), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun InspectorRow(label: String, value: String) {
    Row {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(96.dp),
        )
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun twoDecimals(v: Double): String {
    val scaled = (v * 100).roundToInt()
    return "${scaled / 100}.${(scaled % 100).toString().padStart(2, '0')}"
}
