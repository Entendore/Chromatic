package com.example.chromatic.feature.tools.gradient

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.chromatic.core.color.BlendSpace
import com.example.chromatic.core.color.PaletteExporter
import com.example.chromatic.core.color.Rgb
import com.example.chromatic.core.designsystem.components.CopyableSwatch
import com.example.chromatic.core.designsystem.components.SectionTitle
import com.example.chromatic.core.designsystem.components.toComposeColor

@Composable
fun BlendSpace.label(): String = stringResource(when (this) {
    BlendSpace.SRGB -> R.string.blend_srgb
    BlendSpace.LINEAR_SRGB -> R.string.blend_linear
    BlendSpace.OKLAB -> R.string.blend_oklab
})

@Composable
fun BlendSpace.note(): String = stringResource(when (this) {
    BlendSpace.SRGB -> R.string.blend_srgb_note
    BlendSpace.LINEAR_SRGB -> R.string.blend_linear_note
    BlendSpace.OKLAB -> R.string.blend_oklab_note
})

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradientScreen(
    onShowSnackbar: (String) -> Unit,
    viewModel: GradientViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val clipboard = LocalClipboardManager.current

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Text(
            stringResource(R.string.gradient_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(vertical = 16.dp),
        )

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HexField(stringResource(R.string.from), uiState.fromHex, viewModel::onFromChange, Modifier.weight(1f))
            IconButton(onClick = viewModel::onSwap) {
                Icon(Icons.Filled.SwapHoriz, contentDescription = stringResource(R.string.swap_colors))
            }
            HexField(stringResource(R.string.to), uiState.toHex, viewModel::onToChange, Modifier.weight(1f))
        }

        Spacer(Modifier.height(16.dp))
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            BlendSpace.entries.forEach { space ->
                SegmentedButton(
                    selected = uiState.space == space,
                    onClick = { viewModel.onSpaceChange(space) },
                    shape = SegmentedButtonDefaults.itemShape(index = space.ordinal, count = BlendSpace.entries.size),
                ) { Text(space.label()) }
            }
        }
        Text(
            uiState.space.note(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp),
        )

        if (uiState.isValid) {
            Spacer(Modifier.height(16.dp))
            val colorStops = uiState.stops.mapIndexed { i, rgb ->
                (i / (uiState.stops.size - 1).toFloat()) to rgb.toComposeColor()
            }.toTypedArray()
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.horizontalGradient(*colorStops))
            )

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                listOf(0, 2, 4, 6, 8).forEach { i ->
                    CopyableSwatch(
                        rgb = uiState.stops[i],
                        modifier = Modifier.weight(1f),
                        labelText = if (i == 4) "mid" else "${i * 12}%",
                        onCopied = { hex -> onShowSnackbar("Copied $hex") },
                    )
                }
            }

            SectionTitle(stringResource(R.string.gradient_why))
            Text(
                stringResource(R.string.gradient_why_body),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(12.dp))
            val css = PaletteExporter.cssLinearGradient(uiState.from!!, uiState.to!!, uiState.space)
            FilledTonalButton(onClick = {
                clipboard.setText(AnnotatedString(css))
                onShowSnackbar("CSS copied: $css")
            }) { Text(stringResource(R.string.copy_css_gradient, css)) }
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun HexField(label: String, value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val parsed = Rgb.fromHexOrNull(value)
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = { Text(label) },
        singleLine = true,
        isError = value.isNotEmpty() && parsed == null,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
        leadingIcon = {
            parsed?.let { rgb ->
                Box(Modifier.size(18.dp).clip(CircleShape).background(rgb.toComposeColor()))
            }
        },
    )
}
