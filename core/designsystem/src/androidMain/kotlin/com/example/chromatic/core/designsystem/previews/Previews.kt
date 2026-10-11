package com.example.chromatic.core.designsystem.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview(showBackground = true)
@Composable
fun LabeledSliderPreview() = DesignSystemPreviews.all.first { it.id == "labeled_slider" }.content()

@Preview(showBackground = true)
@Composable
fun CopyableSwatchPreview() = DesignSystemPreviews.all.first { it.id == "copyable_swatch" }.content()

@Preview(showBackground = true)
@Composable
fun PassFailChipsPreview() = DesignSystemPreviews.all.first { it.id == "pass_fail_chips" }.content()

@Preview(showBackground = true)
@Composable
fun ColorWheelPreview() = DesignSystemPreviews.all.first { it.id == "color_wheel" }.content()

@Preview(showBackground = true)
@Composable
fun PalettePreviewCardPreview() = DesignSystemPreviews.all.first { it.id == "palette_preview_card" }.content()

@Preview(showBackground = true)
@Composable
fun FilterChipPreview() = DesignSystemPreviews.all.first { it.id == "filter_chip" }.content()
