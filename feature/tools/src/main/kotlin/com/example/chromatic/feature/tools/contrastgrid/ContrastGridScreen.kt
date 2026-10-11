package com.example.chromatic.feature.tools.contrastgrid

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.chromatic.core.color.ApcaContrast
import com.example.chromatic.core.color.ContrastGrid
import com.example.chromatic.core.color.Hsl
import com.example.chromatic.core.color.PaletteFactory
import com.example.chromatic.core.color.Rgb
import com.example.chromatic.core.color.WcagContrast
import com.example.chromatic.core.designsystem.components.LabeledSlider
import com.example.chromatic.core.designsystem.components.PassFailChip
import com.example.chromatic.core.designsystem.components.SectionTitle
import com.example.chromatic.core.designsystem.components.toComposeColor
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Immutable
data class GridUiState(
    val base: Hsl = Hsl(262f, 0.62f, 0.52f),
    val heatmap: Boolean = false,
    val selected: Pair<Int, Int>? = null,
)

@HiltViewModel
class ContrastGridViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow(GridUiState())
    val state: StateFlow<GridUiState> = _state.asStateFlow()

    fun onHue(value: Float) = _state.update { it.copy(base = it.base.copy(hue = value), selected = null) }
    fun onSaturation(value: Float) = _state.update { it.copy(base = it.base.copy(saturation = value), selected = null) }
    fun onToggleHeatmap() = _state.update { it.copy(heatmap = !it.heatmap) }
    fun onSelect(row: Int, col: Int) = _state.update {
        it.copy(selected = if (it.selected == row to col) null else row to col)
    }
    fun onRandom() {
        val r = Random.Default
        _state.update {
            it.copy(base = Hsl(r.nextFloat() * 360f, 0.45f + r.nextFloat() * 0.55f, 0.5f), selected = null)
        }
    }
}

@Composable
fun ContrastGrid.Grade.label(): String = stringResource(when (this) {
    ContrastGrid.Grade.FAIL -> R.string.grade_fail
    ContrastGrid.Grade.LARGE -> R.string.grade_large
    ContrastGrid.Grade.AA -> R.string.grade_aa
    ContrastGrid.Grade.AAA -> R.string.grade_aaa
})

private fun gradeContainer(grade: ContrastGrid.Grade): Color = when (grade) {
    ContrastGrid.Grade.AAA -> Color(0xFF1B5E20)
    ContrastGrid.Grade.AA -> Color(0xFF43A047)
    ContrastGrid.Grade.LARGE -> Color(0xFFF9A825)
    ContrastGrid.Grade.FAIL -> Color(0xFFC62828)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContrastGridScreen(
    onShowSnackbar: (String) -> Unit,
    viewModel: ContrastGridViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val tones = remember(state.base) { PaletteFactory.tonalScale(state.base.toRgb()) }
    val ratios = remember(tones) { ContrastGrid.ratios(tones) }
    val clipboard = LocalClipboardManager.current

    // Selection changes far less often than recomposition — derived read (v8 §5c).
    val selectedDetail by remember(ratios) {
        derivedStateOf { state.selected?.let { (r, c) -> Triple(r, c, ratios[r][c]) } }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 16.dp)) {
            Text(
                stringResource(R.string.grid_title),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f),
            )
            FilledTonalIconButton(onClick = viewModel::onRandom) {
                Icon(Icons.Filled.Shuffle, contentDescription = stringResource(R.string.random_color))
            }
        }
        Text(
            stringResource(R.string.grid_intro),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(12.dp))
        val aaCount = ratios.sumOf { row -> row.count { it >= WcagContrast.AA_NORMAL } }
        Text(
            pluralStringResource(R.plurals.grid_pairings_pass, aaCount, aaCount),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )

        SectionTitle(stringResource(R.string.axis_hue))
        LabeledSlider(
            label = stringResource(R.string.axis_hue),
            valueText = "${state.base.hue.toInt()}°",
            value = state.base.hue,
            onValueChange = viewModel::onHue,
            valueRange = 0f..360f,
            previewGradient = listOf(
                Color(0xFFFF0000), Color(0xFFFFFF00), Color(0xFF00FF00),
                Color(0xFF00FFFF), Color(0xFF0000FF), Color(0xFFFF00FF), Color(0xFFFF0000),
            ),
        )
        LabeledSlider(
            label = stringResource(R.string.axis_saturation),
            valueText = "${(state.base.saturation * 100).toInt()}%",
            value = state.base.saturation,
            onValueChange = viewModel::onSaturation,
            valueRange = 0f..1f,
            previewGradient = listOf(Color.Gray, Color.hsl(state.base.hue, 1f, 0.5f)),
        )

        SectionTitle(stringResource(R.string.harmony_scheme))
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            listOf(
                false to stringResource(R.string.grid_view_real),
                true to stringResource(R.string.grid_view_heatmap),
            ).forEach { (heatmap, label) ->
                SegmentedButton(
                    selected = state.heatmap == heatmap,
                    onClick = { if (state.heatmap != heatmap) viewModel.onToggleHeatmap() },
                    shape = SegmentedButtonDefaults.itemShape(index = if (heatmap) 1 else 0, count = 2),
                ) { Text(label) }
            }
        }

        Spacer(Modifier.height(16.dp))
        GridHeader(tones)
        tones.forEachIndexed { row, rowTone ->
            GridRow(rowTone, tones, ratios[row], state.heatmap, state.selected, viewModel::onSelect)
        }

        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ContrastGrid.Grade.entries.forEach { grade ->
                Surface(color = gradeContainer(grade), shape = RoundedCornerShape(8.dp)) {
                    Text(
                        grade.label(),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
        }

        selectedDetail?.let { (r, c, ratio) ->
            Spacer(Modifier.height(16.dp))
            SelectedDetail(tones[r], tones[c], ratio, onCopyPair = {
                val pair = "${tones[r].rgb.hex} on ${tones[c].rgb.hex}"
                clipboard.setText(AnnotatedString(pair))
                onShowSnackbar("Copied $pair")
            })
        }

        Text(
            stringResource(R.string.grid_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp),
        )
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun GridHeader(tones: List<PaletteFactory.Tone>) {
    Row(Modifier.fillMaxWidth()) {
        Spacer(Modifier.width(30.dp))
        tones.forEach { tone ->
            Text(
                "${tone.level}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun GridRow(
    rowTone: PaletteFactory.Tone,
    tones: List<PaletteFactory.Tone>,
    ratios: List<Double>,
    heatmap: Boolean,
    selected: Pair<Int, Int>?,
    onSelect: (Int, Int) -> Unit,
) {
    Row(Modifier.fillMaxWidth().padding(top = 2.dp)) {
        Text(
            "${rowTone.level}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(30.dp),
        )
        tones.forEachIndexed { col, colTone ->
            GridCell(
                fg = rowTone.rgb, bg = colTone.rgb,
                fgLevel = rowTone.level, bgLevel = colTone.level,
                ratio = ratios[col],
                heatmap = heatmap,
                selected = selected == rowTone.level to colTone.level,
                onClick = { onSelect(rowTone.level, colTone.level) },
                modifier = Modifier.weight(1f).padding(1.dp),
            )
        }
    }
}

@Composable
private fun GridCell(
    fg: Rgb,
    bg: Rgb,
    fgLevel: Int,
    bgLevel: Int,
    ratio: Double,
    heatmap: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val container = if (heatmap) gradeContainer(ContrastGrid.grade(ratio)) else bg.toComposeColor()
    // Localized description from tone LEVELS — never interpolated data-class objects (v13 §0b).
    val desc = stringResource(
        R.string.grid_cell_a11y, fgLevel, bgLevel,
        "%.1f".format(ratio), ContrastGrid.grade(ratio).label(),
    )
    Box(
        modifier
            .height(30.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(container)
            .then(
                if (selected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                else Modifier
            )
            .clickable(onClick = onClick)
            .semantics { contentDescription = desc },
        contentAlignment = Alignment.Center,
    ) {
        if (heatmap) {
            Text("%.0f".format(ratio), style = MaterialTheme.typography.labelSmall, color = Color.White)
        } else {
            Text("Aa", style = MaterialTheme.typography.labelMedium, color = fg.toComposeColor())
        }
    }
}

@Composable
private fun SelectedDetail(
    textTone: PaletteFactory.Tone,
    backgroundTone: PaletteFactory.Tone,
    ratio: Double,
    onCopyPair: () -> Unit,
) {
    val report = WcagContrast.report(textTone.rgb, backgroundTone.rgb)
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Tone ${textTone.level} → ${backgroundTone.level}", style = MaterialTheme.typography.titleSmall)
            Text(report.formatted, style = MaterialTheme.typography.headlineSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                PassFailChip(ContrastGrid.grade(ratio).label(), ContrastGrid.grade(ratio).passesBodyText)
                PassFailChip("AA large", report.passesAaLarge)
                Text(
                    "APCA ≈ ${abs(ApcaContrast.lightnessContrast(textTone.rgb, backgroundTone.rgb)).roundToInt()} Lc",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (textTone.level == backgroundTone.level) {
                Text(stringResource(R.string.grid_same_tone), style = MaterialTheme.typography.bodySmall)
            }
            OutlinedButton(onClick = onCopyPair) {
                Text(stringResource(R.string.grid_copy_pair, textTone.rgb.hex, backgroundTone.rgb.hex))
            }
        }
    }
}
