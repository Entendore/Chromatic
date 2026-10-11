package com.example.chromatic.feature.tools.palette

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.chromatic.core.color.ColorHarmony
import com.example.chromatic.core.color.ColorNamer
import com.example.chromatic.core.color.PaletteStyle
import com.example.chromatic.core.color.Rgb
import com.example.chromatic.core.color.toHsv
import com.example.chromatic.core.data.model.SavedPalette
import com.example.chromatic.core.designsystem.components.CopyableSwatch
import com.example.chromatic.core.designsystem.components.ExportDialog
import com.example.chromatic.core.designsystem.components.LabeledSlider
import com.example.chromatic.core.designsystem.components.SectionTitle
import com.example.chromatic.core.designsystem.components.toComposeColor
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.collectLatest

@Composable
fun PaletteStyle.label(): String = stringResource(when (this) {
    PaletteStyle.PASTEL -> R.string.style_pastel
    PaletteStyle.NEON -> R.string.style_neon
    PaletteStyle.EARTH -> R.string.style_earth
    PaletteStyle.JEWEL -> R.string.style_jewel
    PaletteStyle.MONOCHROME -> R.string.style_mono
    PaletteStyle.VINTAGE -> R.string.style_vintage
})

@Composable
fun PaletteStyle.displayName(): String = "${label()} mood"

/** Action-capable snackbar (v6 §7): message + optional action + handler. */
typealias SnackbarWithAction = (String, String?, (() -> Unit)?) -> Unit

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PaletteBuilderScreen(
    onShowSnackbar: SnackbarWithAction,
    onSendToThemeStudio: (com.example.chromatic.core.color.Hsl) -> Unit = {},
    viewModel: PaletteBuilderViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val baseColor = state.baseHsl.toRgb().toComposeColor()
    var exportTarget by remember { mutableStateOf<SavedPalette?>(null) }
    exportTarget?.let { palette ->
        ExportDialog(
            paletteName = palette.name,
            colors = palette.hexColors.mapNotNull(Rgb::fromHexOrNull),
            onDismiss = { exportTarget = null },
            onShowSnackbar = { msg -> onShowSnackbar(msg, null, null) },
            onSharePng = { viewModel.onSharePng(palette) },
        )
    }

    // PNG share result (v9 §3): instant chooser here, suspend work lived in the VM.
    LaunchedEffect(Unit) {
        viewModel.shareUri.collectLatest { uri ->
            uri?.let {
                val share = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, it)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(share, null))
                viewModel.onShareConsumed()
            }
        }
    }

    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let(viewModel::onImagePicked)
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 16.dp)) {
            Text(
                stringResource(R.string.builder_title),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f),
            )
            FilledTonalIconButton(onClick = viewModel::onRandomBase) {
                Icon(Icons.Filled.Shuffle, contentDescription = stringResource(R.string.random_color))
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(56.dp).clip(CircleShape).background(baseColor))
            Column {
                Text(state.baseHsl.toRgb().hex, style = MaterialTheme.typography.titleMedium)
                Text(
                    "≈ ${ColorNamer.nameOf(state.baseHsl.toRgb())}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        LabeledSlider(
            label = stringResource(R.string.axis_hue),
            valueText = "${state.baseHsl.hue.roundToInt()}°",
            value = state.baseHsl.hue, onValueChange = viewModel::onHueChange, valueRange = 0f..360f,
            previewGradient = listOf(
                Color(0xFFFF0000), Color(0xFFFFFF00), Color(0xFF00FF00),
                Color(0xFF00FFFF), Color(0xFF0000FF), Color(0xFFFF00FF), Color(0xFFFF0000),
            ),
        )
        LabeledSlider(
            label = stringResource(R.string.axis_saturation),
            valueText = "${(state.baseHsl.saturation * 100).roundToInt()}%",
            value = state.baseHsl.saturation, onValueChange = viewModel::onSaturationChange, valueRange = 0f..1f,
            previewGradient = listOf(Color.Gray, Color.hsl(state.baseHsl.hue, 1f, 0.5f)),
        )
        LabeledSlider(
            label = stringResource(R.string.axis_lightness),
            valueText = "${(state.baseHsl.lightness * 100).roundToInt()}%",
            value = state.baseHsl.lightness, onValueChange = viewModel::onLightnessChange, valueRange = 0f..1f,
            previewGradient = listOf(Color.Black, baseColor, Color.White),
        )

        OutlinedButton(
            onClick = { onSendToThemeStudio(state.baseHsl) },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        ) { Text(stringResource(R.string.send_to_theme_studio)) }

        SectionTitle(stringResource(R.string.tonal_scale))
        viewModel.tonesFor(state.baseHsl).chunked(5).forEach { rowTones ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                rowTones.forEach { tone ->
                    CopyableSwatch(
                        rgb = tone.rgb,
                        labelText = "${tone.level}",
                        modifier = Modifier.weight(1f),
                        onCopied = { hex -> onShowSnackbar("Copied $hex".let { it }, null, null) },
                    )
                }
            }
        }

        SectionTitle(stringResource(R.string.harmony_variations))
        listOf(
            ColorHarmony.COMPLEMENTARY, ColorHarmony.TRIADIC, ColorHarmony.ANALOGOUS,
        ).forEach { harmony ->
            Text(harmony.name.lowercase().replaceFirstChar(Char::uppercaseChar), style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 12.dp)) {
                harmony.generate(state.baseHsl.toHsv()).map { it.toRgb() }.forEach { rgb ->
                    CopyableSwatch(
                        rgb = rgb,
                        modifier = Modifier.weight(1f),
                        onCopied = { hex -> onShowSnackbar("Copied $hex", null, null) },
                    )
                }
            }
        }

        SectionTitle(stringResource(R.string.extract_from_photo))
        OutlinedButton(onClick = {
            photoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }) {
            Icon(Icons.Filled.Image, contentDescription = null, Modifier.size(18.dp))
            Spacer(Modifier.size(8.dp))
            Text(stringResource(R.string.choose_photo))
        }
        if (state.extracted.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                state.extracted.forEach { swatch ->
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        CopyableSwatch(
                            rgb = swatch.rgb,
                            labelText = swatch.name,
                            onClick = { viewModel.onExtractedColorSelected(swatch.rgb) },
                        )
                        Text(
                            stringResource(R.string.percent_of_image, (swatch.fraction * 100).roundToInt()),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Text(
                stringResource(R.string.extract_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        SectionTitle(stringResource(R.string.style_palettes))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            PaletteStyle.entries.forEach { style ->
                FilterChip(
                    selected = state.styleResult?.first == style,
                    onClick = { viewModel.onStyleGenerated(style) },
                    label = { Text(style.label()) },
                )
            }
        }
        state.styleResult?.let { (_, colors) ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                colors.forEach { rgb ->
                    CopyableSwatch(rgb = rgb, modifier = Modifier.weight(1f), onCopied = { onShowSnackbar("Copied $it", null, null) })
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 8.dp)) {
                OutlinedButton(
                    onClick = { viewModel.onStyleGenerated(state.styleResult!!.first) },
                    modifier = Modifier.weight(1f),
                ) { Text(stringResource(R.string.regenerate)) }
                Button(
                    onClick = { viewModel.saveStylePalette(state.styleResult!!.first.displayName()) },
                    modifier = Modifier.weight(1f),
                ) { Text(stringResource(R.string.save)) }
            }
        }

        SectionTitle(stringResource(R.string.saved_palettes))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                label = { Text(stringResource(R.string.name_label)) },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            Button(onClick = viewModel::savePalette) { Text(stringResource(R.string.save)) }
        }

        state.savedPalettes.forEach { palette ->
            ElevatedCard(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(palette.name, style = MaterialTheme.typography.titleSmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            palette.hexColors.take(10).forEach { hex ->
                                Box(
                                    Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(Rgb.fromHexOrNull(hex)?.toComposeColor() ?: Color.Gray)
                                )
                            }
                        }
                    }
                    IconButton(onClick = { exportTarget = palette }) {
                        Icon(Icons.Filled.IosShare, contentDescription = "Export ${palette.name}")
                    }
                    IconButton(onClick = {
                        // Bounded undo stack (v12 §0c) — the snackbar action restores.
                        viewModel.deletePalette(palette.id)
                        onShowSnackbar(
                            palette.name.let { context.getString(R.string.palette_deleted, it) },
                            context.getString(R.string.undo),
                        ) { viewModel.undoDelete() }
                    }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete ${palette.name}")
                    }
                }
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}
