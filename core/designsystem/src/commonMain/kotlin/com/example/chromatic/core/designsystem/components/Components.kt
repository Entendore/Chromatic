package com.example.chromatic.core.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import chromatic.designsystem.res.Res
import chromatic.designsystem.res.chip_fails
import chromatic.designsystem.res.chip_passes
import chromatic.designsystem.res.copy_hex_code
import chromatic.designsystem.res.inspect_color
import chromatic.designsystem.res.swatch_selected
import com.example.chromatic.core.color.Rgb
import org.jetbrains.compose.resources.stringResource

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier.padding(top = 20.dp, bottom = 8.dp),
    )
}

/** Material slider with an optional gradient strip. [testTag] for flow tests (v12). */
@Composable
fun LabeledSlider(
    label: String,
    valueText: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    previewGradient: List<Color>? = null,
    modifier: Modifier = Modifier,
    testTag: String? = null,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.weight(1f))
            Text(
                valueText,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (previewGradient != null) {
            Spacer(Modifier.height(6.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Brush.horizontalGradient(previewGradient))
            )
            Spacer(Modifier.height(2.dp))
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = testTag?.let { Modifier.testTag(it) } ?: Modifier,
        )
    }
}

/**
 * Swatch that copies its hex on tap (haptic confirmed), long-press inspects (v3/v10),
 * announces its actions and selection state to assistive tech (v6/v11).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CopyableSwatch(
    rgb: Rgb,
    modifier: Modifier = Modifier,
    labelText: String = rgb.hex,
    selected: Boolean = false,
    showLabel: Boolean = true,
    onClick: (() -> Unit)? = null,
    onCopied: ((String) -> Unit)? = null,
    onInfo: (() -> Unit)? = null,
    testTag: String? = null,
) {
    val clipboard = LocalClipboardManager.current
    val haptics = LocalHapticFeedback.current
    val borderColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "swatchBorder",
    )
    val copyLabel = stringResource(Res.string.copy_hex_code)
    val inspectLabel = stringResource(Res.string.inspect_color)
    val selectedLabel = stringResource(Res.string.swatch_selected)

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(rgb.toComposeColor())
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .then(testTag?.let { Modifier.testTag(it) } ?: Modifier)
            .combinedClickable(
                onClickLabel = copyLabel,
                onLongClickLabel = if (onInfo != null) inspectLabel else null,
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick?.invoke()
                    clipboard.setText(AnnotatedString(rgb.hex))
                    onCopied?.invoke(rgb.hex)
                },
                onLongClick = onInfo,
            )
            .semantics { if (selected) stateDescription = selectedLabel },
        contentAlignment = Alignment.BottomCenter,
    ) {
        if (onInfo != null) {
            Text(
                "ⓘ",
                style = MaterialTheme.typography.labelSmall,
                color = readableTextColor(rgb),
                modifier = Modifier.align(Alignment.TopEnd).padding(6.dp),
            )
        }
        if (showLabel) {
            Text(
                text = labelText,
                style = MaterialTheme.typography.labelSmall,
                color = readableTextColor(rgb),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.18f))
                    .padding(vertical = 3.dp, horizontal = 4.dp),
            )
        }
    }
}

/** Theme-aware pass/fail chip; the verdict is a contentDescription (v3/v6/v11). */
@Composable
fun PassFailChip(label: String, passes: Boolean) {
    val verdict = stringResource(if (passes) Res.string.chip_passes else Res.string.chip_fails)
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (passes) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
        contentColor = if (passes) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
    ) {
        Text(
            text = (if (passes) "✓ " else "✕ ") + label,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .semantics { contentDescription = "$label — $verdict" },
        )
    }
}
