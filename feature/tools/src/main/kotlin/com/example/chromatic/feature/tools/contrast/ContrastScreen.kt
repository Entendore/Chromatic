package com.example.chromatic.feature.tools.contrast

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.chromatic.core.color.ApcaContrast
import com.example.chromatic.core.color.Rgb
import com.example.chromatic.core.color.WcagContrast
import com.example.chromatic.core.designsystem.components.ColorPickerSheet
import com.example.chromatic.core.designsystem.components.PassFailChip
import com.example.chromatic.core.designsystem.components.SectionTitle
import com.example.chromatic.core.designsystem.components.toComposeColor
import kotlin.math.abs
import kotlin.math.roundToInt

private enum class PickTarget { FG, BG }

@Composable
fun ContrastGridGradeLabelUnused() = Unit // placeholder removed below

@Composable
fun ContrastScreen(
    onShowSnackbar: (String) -> Unit,
    onOpenVision: () -> Unit = {},
    viewModel: ContrastViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var picking by remember { mutableStateOf<PickTarget?>(null) }

    picking?.let { target ->
        val initial = if (target == PickTarget.FG) uiState.foreground else uiState.background
        initial?.let {
            ColorPickerSheet(
                initial = it,
                onDismiss = { picking = null },
                onSelected = { rgb ->
                    if (target == PickTarget.FG) viewModel.onPickForeground(rgb)
                    else viewModel.onPickBackground(rgb)
                    picking = null
                },
            )
        }
    }

    // Memoized report (v9 §0a): recomputed only when a resolved color changes.
    val report = remember(uiState.foreground, uiState.background) {
        if (uiState.isValid) WcagContrast.report(uiState.foreground!!, uiState.background!!) else null
    }
    val apcaLc = remember(uiState.foreground, uiState.background) {
        if (uiState.isValid) ApcaContrast.lightnessContrast(uiState.foreground!!, uiState.background!!) else 0.0
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Text(
            stringResource(R.string.contrast_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(vertical = 16.dp),
        )

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HexField(
                label = stringResource(R.string.foreground),
                value = uiState.foregroundHex,
                onValueChange = viewModel::onForegroundHexChange,
                onPick = { picking = PickTarget.FG },
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = viewModel::onSwap) {
                Icon(Icons.Filled.SwapHoriz, contentDescription = stringResource(R.string.swap_colors))
            }
            HexField(
                label = stringResource(R.string.background),
                value = uiState.backgroundHex,
                onValueChange = viewModel::onBackgroundHexChange,
                onPick = { picking = PickTarget.BG },
                modifier = Modifier.weight(1f),
            )
        }

        report?.let { r ->
            Spacer(Modifier.height(16.dp))
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(r.formatted, style = MaterialTheme.typography.headlineLarge)
                        Spacer(Modifier.size(8.dp))
                        Text(
                            stringResource(R.string.contrast_ratio),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        PassFailChip("AA", r.passesAaNormal)
                        PassFailChip("AA large", r.passesAaLarge)
                        PassFailChip("AAA", r.passesAaaNormal)
                        PassFailChip("AAA large", r.passesAaaLarge)
                        PassFailChip("UI 3:1", r.passesUiComponents)
                    }
                    HorizontalDivider()
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("${abs(apcaLc).roundToInt()} Lc", style = MaterialTheme.typography.titleMedium)
                        Text(
                            stringResource(
                                if (apcaLc == 0.0) R.string.apca_note
                                else if (apcaLc > 0) R.string.apca_polarity_dark
                                else R.string.apca_polarity_light
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        stringResource(R.string.apca_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            SectionTitle(stringResource(R.string.live_preview))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = uiState.background!!.toComposeColor(),
                    contentColor = uiState.foreground!!.toComposeColor(),
                ),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Headline sample text", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        "Body copy at reading size. Everything on this card is rendered with your exact colors.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text("Caption / metadata", style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(Modifier.height(12.dp))
            FilledTonalButton(onClick = viewModel::onFixForeground, enabled = uiState.isValid) {
                Text(stringResource(R.string.auto_fix_aa))
            }
            uiState.lastFixHex?.let { fixed ->
                Text(
                    stringResource(R.string.fix_feedback, fixed, r.formatted),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            OutlinedButton(onClick = onOpenVision, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Text(stringResource(R.string.check_vision))
            }
        }

        Spacer(Modifier.height(24.dp))
        Text(
            stringResource(R.string.wcag_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun HexField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onPick: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
        trailingIcon = {
            IconButton(onClick = onPick) {
                Icon(Icons.Filled.Colorize, contentDescription = stringResource(R.string.pick_color))
            }
        },
    )
}
