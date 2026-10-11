package com.example.chromatic.feature.tools.themestudio

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
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.chromatic.core.color.ColorNamer
import com.example.chromatic.core.color.Rgb
import com.example.chromatic.core.color.ThemeGenerator
import com.example.chromatic.core.color.toRgb
import com.example.chromatic.core.designsystem.components.LabeledSlider
import com.example.chromatic.core.designsystem.components.PassFailChip
import com.example.chromatic.core.designsystem.components.SectionTitle
import com.example.chromatic.core.designsystem.components.toComposeColor
import com.example.chromatic.core.designsystem.components.readableTextColor
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ThemeStudioScreen(
    onShowSnackbar: (String) -> Unit,
    viewModel: ThemeStudioViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // Heavy derivation memoized against the seed only (v6 §0c).
    val bundle = remember(state.seed) { ThemeGenerator.generate(state.seed.toRgb()) }
    val scheme = if (state.darkPreview) bundle.dark else bundle.light
    val seedRgb = state.seed.toRgb()
    val clipboard = LocalClipboardManager.current
    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let(viewModel::onImagePicked)
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Text(
            stringResource(R.string.studio_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(vertical = 16.dp),
        )
        Text(
            stringResource(R.string.studio_intro),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(top = 16.dp),
        ) {
            Box(Modifier.size(56.dp).clip(CircleShape).background(seedRgb.toComposeColor()))
            Column {
                Text(seedRgb.hex, style = MaterialTheme.typography.titleMedium)
                Text(
                    "≈ ${ColorNamer.nameOf(seedRgb)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.weight(1f))
            FilledTonalIconButton(onClick = viewModel::onRandomColor) {
                Icon(Icons.Filled.Shuffle, contentDescription = stringResource(R.string.random_color))
            }
        }

        LabeledSlider(
            label = stringResource(R.string.seed_hue),
            valueText = "${state.seed.hue.roundToInt()}°",
            value = state.seed.hue,
            onValueChange = viewModel::onHueChange,
            valueRange = 0f..360f,
            previewGradient = listOf(
                Color(0xFFFF0000), Color(0xFFFFFF00), Color(0xFF00FF00),
                Color(0xFF00FFFF), Color(0xFF0000FF), Color(0xFFFF00FF), Color(0xFFFF0000),
            ),
        )
        LabeledSlider(
            label = stringResource(R.string.seed_saturation),
            valueText = "${(state.seed.saturation * 100).roundToInt()}%",
            value = state.seed.saturation,
            onValueChange = viewModel::onSaturationChange,
            valueRange = 0f..1f,
            previewGradient = listOf(Color.Gray, Color.hsl(state.seed.hue, 1f, 0.5f)),
        )

        SectionTitle(stringResource(R.string.seed_from_photo))
        OutlinedButton(onClick = {
            photoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }) {
            Icon(Icons.Filled.Image, contentDescription = null, Modifier.size(18.dp))
            Spacer(Modifier.size(8.dp))
            Text(stringResource(R.string.choose_photo))
        }
        if (state.extracting) {
            Text("…", style = MaterialTheme.typography.bodySmall)
        }
        if (state.extracted.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                state.extracted.forEach { swatch ->
                    Column(Modifier.weight(1f)) {
                        com.example.chromatic.core.designsystem.components.CopyableSwatch(
                            rgb = swatch.rgb,
                            labelText = swatch.name,
                            onClick = { viewModel.onSeedFromSwatch(swatch.rgb) },
                        )
                        Text(
                            stringResource(R.string.percent_of_image, (swatch.fraction * 100).roundToInt()),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        SectionTitle(stringResource(R.string.scheme_preview))
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            listOf(false to stringResource(R.string.light_label), true to stringResource(R.string.dark_label))
                .forEach { (dark, label) ->
                    SegmentedButton(
                        selected = state.darkPreview == dark,
                        onClick = { viewModel.onDarkPreview(dark) },
                        shape = SegmentedButtonDefaults.itemShape(index = if (dark) 1 else 0, count = 2),
                    ) { Text(label) }
                }
        }

        Spacer(Modifier.height(12.dp))
        scheme.roles.entries.chunked(2).forEach { rowRoles ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                rowRoles.forEach { (role, rgb) -> RoleTile(role, rgb, Modifier.weight(1f)) }
                if (rowRoles.size == 1) Spacer(Modifier.weight(1f))
            }
        }

        SectionTitle(stringResource(R.string.contrast_verification))
        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                scheme.verifications.forEach { check ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(check.label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        Text(
                            "%.2f:1".format(check.ratio),
                            style = MaterialTheme.typography.labelMedium,
                        )
                        PassFailChip("AA", check.passes)
                    }
                }
                Text(
                    stringResource(R.string.verified_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                viewModel.onApplyTheme()
                onShowSnackbar(themeAppliedMessage())
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(R.string.apply_to_chromatic)) }

        Spacer(Modifier.height(8.dp))
        FilledTonalButton(
            onClick = {
                clipboard.setText(AnnotatedString(ThemeGenerator.toComposeCode(bundle, "Brand")))
                onShowSnackbar(copiedMessage())
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(R.string.copy_theme_code)) }

        Text(
            stringResource(R.string.studio_how),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun themeAppliedMessage(): String = stringResource(R.string.theme_applied)

@Composable
private fun copiedMessage(): String = stringResource(R.string.theme_code_copied)

@Composable
private fun RoleTile(name: String, rgb: Rgb, modifier: Modifier = Modifier) {
    Surface(modifier, color = rgb.toComposeColor(), shape = RoundedCornerShape(12.dp)) {
        Text(
            text = "$name\n${rgb.hex}",
            style = MaterialTheme.typography.labelSmall,
            color = readableTextColor(rgb),
            modifier = Modifier.padding(10.dp),
        )
    }
}
