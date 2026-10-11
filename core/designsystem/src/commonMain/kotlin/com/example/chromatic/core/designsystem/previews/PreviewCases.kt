package com.example.chromatic.core.designsystem.previews

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.chromatic.core.designsystem.components.ColorWheel
import com.example.chromatic.core.designsystem.components.CopyableSwatch
import com.example.chromatic.core.designsystem.components.LabeledSlider
import com.example.chromatic.core.designsystem.components.PalettePreviewCard
import com.example.chromatic.core.designsystem.components.PassFailChip
import com.example.chromatic.core.designsystem.theme.ChromaticTheme
import com.example.chromatic.core.color.Hsv
import com.example.chromatic.core.color.Rgb

/** One declaration → IDE preview AND CI pixel gate (v15 §2). */
data class PreviewCase(val id: String, val content: @Composable () -> Unit)

object DesignSystemPreviews {
    val all = listOf(
        PreviewCase("labeled_slider") {
            ChromaticTheme(dynamicColor = false) {
                LabeledSlider(
                    "Hue", "210°", 0.58f, {}, 0f..1f,
                    previewGradient = listOf(Color(0xFF0000FF), Color(0xFF00FF00)),
                )
            }
        },
        PreviewCase("copyable_swatch") {
            ChromaticTheme(dynamicColor = false) {
                CopyableSwatch(Rgb(90, 140, 210), Modifier.width(120.dp))
            }
        },
        PreviewCase("pass_fail_chips") {
            ChromaticTheme(dynamicColor = false) {
                Row(Modifier.padding(8.dp)) {
                    PassFailChip("AA", true)
                    PassFailChip("AAA", false)
                }
            }
        },
        PreviewCase("color_wheel") {
            ChromaticTheme(dynamicColor = false) {
                ColorWheel(
                    hsvProvider = { Hsv(210f, 0.6f, 0.9f) },
                    onSelected = { _, _ -> },
                    modifier = Modifier.size(160.dp),
                )
            }
        },
        PreviewCase("palette_preview_card") {
            ChromaticTheme(dynamicColor = false) {
                PalettePreviewCard(
                    listOf(Rgb(30, 90, 220), Rgb(220, 60, 90), Rgb(240, 180, 40)),
                    Modifier.fillMaxWidth().padding(8.dp),
                )
            }
        },
        PreviewCase("filter_chip") {
            ChromaticTheme(dynamicColor = false) {
                Row(Modifier.padding(8.dp)) {
                    FilterChip(selected = true, onClick = {}, label = { Text("Complementary") })
                }
            }
        },
    )
}
