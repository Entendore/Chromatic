package com.example.chromatic.feature.tools.harmony

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.chromatic.core.color.ColorConverter
import com.example.chromatic.core.color.ColorHarmony
import com.example.chromatic.core.color.HarmonyMode
import com.example.chromatic.core.color.Hsv
import com.example.chromatic.core.color.Rgb
import com.example.chromatic.core.designsystem.components.ColorInspectorSheet
import com.example.chromatic.core.designsystem.components.ColorWheel
import com.example.chromatic.core.designsystem.components.CopyableSwatch
import com.example.chromatic.core.designsystem.components.LabeledSlider
import com.example.chromatic.core.designsystem.components.PalettePreviewCard
import com.example.chromatic.core.designsystem.components.SectionTitle
import com.example.chromatic.core.designsystem.components.toComposeColor
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ColorHarmony.title(): String = stringResource(when (this) {
    ColorHarmony.COMPLEMENTARY -> R.string.harmony_complementary
    ColorHarmony.ANALOGOUS -> R.string.harmony_analogous
    ColorHarmony.TRIADIC -> R.string.harmony_triadic
    ColorHarmony.SPLIT_COMPLEMENTARY -> R.string.harmony_split
    ColorHarmony.TETRADIC -> R.string.harmony_tetradic
    ColorHarmony.MONOCHROMATIC -> R.string.harmony_mono
})

@Composable
fun ColorHarmony.description(): String = stringResource(when (this) {
    ColorHarmony.COMPLEMENTARY -> R.string.harmony_complementary_desc
    ColorHarmony.ANALOGOUS -> R.string.harmony_analogous_desc
    ColorHarmony.TRIADIC -> R.string.harmony_triadic_desc
    ColorHarmony.SPLIT_COMPLEMENTARY -> R.string.harmony_split_desc
    ColorHarmony.TETRADIC -> R.string.harmony_tetradic_desc
    ColorHarmony.MONOCHROMATIC -> R.string.harmony_mono_desc
})

@Composable
fun HarmonyMode.label(): String = stringResource(if (this == HarmonyMode.TRADITIONAL) R.string.mode_hsv else R.string.mode_oklch)

@Composable
fun HarmonyMode.note(): String = stringResource(if (this == HarmonyMode.TRADITIONAL) R.string.mode_hsv_note else R.string.mode_oklch_note)

/**
 * Drag input subtree (v8 §3): fast-moving floats stay local; the ViewModel receives
 * them via a conflated snapshotFlow — gesture frames never wait for regeneration.
 * Includes a precision hue slider (tag `harmony.slider.hue`) for accessibility
 * and flow tests.
 */
@Composable
private fun HarmonyControls(
    initial: Hsv,
    onPaletteInput: (Float, Float, Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    var hue by remember { mutableFloatStateOf(initial.hue) }
    var saturation by remember { mutableFloatStateOf(initial.saturation) }
    var value by remember { mutableFloatStateOf(initial.value.coerceIn(0.1f, 1f)) }

    LaunchedEffect(Unit) {
        snapshotFlow { Triple(hue, saturation, value) }
            .conflate()
            .collectLatest { (h, s, v) -> onPaletteInput(h, s, v) }
    }

    Column(modifier) {
        ColorWheel(
            hsvProvider = { Hsv(hue, saturation, value) },
            onSelected = { h, s -> hue = h; saturation = s },
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.CenterHorizontally),
        )
        Text(
            stringResource(R.string.harmony_wheel_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )
        LabeledSlider(
            label = stringResource(R.string.axis_hue),
            valueText = "${hue.roundToInt()}°",
            value = hue,
            onValueChange = { hue = it },
            valueRange = 0f..360f,
            previewGradient = listOf(
                Color(0xFFFF0000), Color(0xFFFFFF00), Color(0xFF00FF00),
                Color(0xFF00FFFF), Color(0xFF0000FF), Color(0xFFFF00FF), Color(0xFFFF0000),
            ),
            testTag = "harmony.slider.hue",
        )
        LabeledSlider(
            label = stringResource(R.string.axis_lightness_value),
            valueText = "${(value * 100).toInt()}%",
            value = value,
            onValueChange = { value = it.coerceIn(0.1f, 1f) },
            valueRange = 0.1f..1f,
            previewGradient = listOf(
                Color.Black,
                Color.hsv(hue, saturation, value),
                Color.White,
            ),
        )
        FilledTonalIconButton(
            onClick = {
                val r = kotlin.random.Random.Default
                hue = r.nextFloat() * 360f
                saturation = 0.35f + r.nextFloat() * 0.65f
                value = 0.75f + r.nextFloat() * 0.25f
            },
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Icon(Icons.Filled.Shuffle, contentDescription = stringResource(R.string.random_color))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HarmonyScreen(
    onShowSnackbar: (String) -> Unit,
    viewModel: HarmonyViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cvdResult by viewModel.cvdResult.collectAsState()
    var inspected by remember { mutableStateOf<Rgb?>(null) }
    inspected?.let { ColorInspectorSheet(it) { inspected = null } }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Text(
            stringResource(R.string.harmony_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(vertical = 16.dp),
        )

        HarmonyControls(
            initial = uiState.baseHsv,
            onPaletteInput = viewModel::onPaletteInput,
        )

        SectionTitle(stringResource(R.string.harmony_scheme))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(ColorHarmony.entries) { harmony ->
                FilterChip(
                    selected = uiState.harmony == harmony,
                    onClick = { viewModel.onHarmonySelected(harmony) },
                    label = { Text(harmony.title()) },
                )
            }
        }
        Text(
            uiState.harmony.description(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )

        SectionTitle(stringResource(R.string.hue_spacing))
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            HarmonyMode.entries.forEach { mode ->
                SegmentedButton(
                    selected = uiState.mode == mode,
                    onClick = { viewModel.onModeSelected(mode) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = mode.ordinal, count = HarmonyMode.entries.size,
                    ),
                ) { Text(mode.label()) }
            }
        }
        Text(
            uiState.mode.note(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )

        SectionTitle(stringResource(R.string.harmony_palette))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            uiState.palette.forEachIndexed { index, rgb ->
                CopyableSwatch(
                    rgb = rgb,
                    modifier = Modifier.weight(1f),
                    selected = index == 0,
                    onCopied = { hex -> onShowSnackbar(stringResource(R.string.copied, hex)) },
                    onInfo = { inspected = rgb },
                    testTag = "harmony.swatch.$index",
                )
            }
        }
        Text(
            stringResource(R.string.inspected_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )

        FilledTonalButton(
            onClick = viewModel::onToggleCvdSafe,
            modifier = Modifier.padding(top = 12.dp),
        ) {
            Text(
                if (cvdResult == null) stringResource(R.string.preview_cvd_safe)
                else stringResource(R.string.hide_cvd_safe)
            )
        }
        cvdResult?.let { result ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                result.colors.forEach { rgb ->
                    CopyableSwatch(
                        rgb = rgb,
                        modifier = Modifier.weight(1f),
                        onCopied = { hex -> onShowSnackbar(stringResource(R.string.copied, hex)) },
                    )
                }
            }
            Text(
                stringResource(
                    R.string.cvd_safe_result,
                    result.adjustmentsMade,
                    twoDecimals(result.minSimulatedDeltaE),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp),
            )
        }

        SectionTitle(stringResource(R.string.harmony_applied))
        PalettePreviewCard(uiState.palette)
        Spacer(Modifier.height(32.dp))
    }
}

internal fun twoDecimals(v: Double): String {
    val scaled = (v * 100).toInt()
    return "${scaled / 100}.${(scaled % 100).toString().padStart(2, '0')}"
}
