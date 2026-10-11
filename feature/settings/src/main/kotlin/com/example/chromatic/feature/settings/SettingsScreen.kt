package com.example.chromatic.feature.settings

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.chromatic.core.color.ColorVisionType
import com.example.chromatic.core.data.repository.ThemeMode
import com.example.chromatic.core.designsystem.components.SectionTitle
import com.example.chromatic.core.designsystem.components.label

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenCrashReports: () -> Unit,
    onOpenHelp: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
            }
            Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineSmall)
        }

        SectionTitle(stringResource(R.string.appearance))
        ThemeMode.entries.forEach { mode ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            ) {
                RadioButton(
                    selected = settings.themeMode == mode,
                    onClick = { viewModel.onThemeModeSelected(mode) },
                )
                Text(
                    stringResource(when (mode) {
                        ThemeMode.SYSTEM -> R.string.theme_follow_system
                        ThemeMode.LIGHT -> R.string.theme_light
                        ThemeMode.DARK -> R.string.theme_dark
                    }),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
        if (settings.appliedThemeSeedHex != null) {
            Text(
                stringResource(R.string.applied_theme_custom),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
            OutlinedButton(onClick = viewModel::onResetTheme, modifier = Modifier.padding(top = 4.dp)) {
                Text(stringResource(R.string.reset_theme))
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        ) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.dynamic_color), style = MaterialTheme.typography.titleSmall)
                Text(
                    stringResource(R.string.dynamic_color_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = settings.dynamicColor, onCheckedChange = viewModel::onDynamicColorChanged)
        }

        SectionTitle(stringResource(R.string.vision_preview))
        Text(
            stringResource(R.string.vision_preview_desc),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        @OptIn(ExperimentalLayoutApi::class)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FilterChip(
                selected = settings.cvdPreview == null,
                onClick = { viewModel.onCvdPreviewSelected(null) },
                label = { Text(stringResource(R.string.preview_off)) },
            )
            ColorVisionType.entries.filter { it != ColorVisionType.NORMAL }.forEach { type ->
                FilterChip(
                    selected = settings.cvdPreview == type,
                    onClick = { viewModel.onCvdPreviewSelected(type) },
                    label = { Text(type.label()) },
                )
            }
        }

        SectionTitle(stringResource(R.string.about))
        Text(
            stringResource(R.string.about_body),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Chromatic ${viewModel.buildInfo.versionName} (${viewModel.buildInfo.versionCode})",
            style = MaterialTheme.typography.labelMedium,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            stringResource(R.string.fully_offline),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onOpenCrashReports, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.crash_reports))
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onOpenHelp, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.help))
        }
        Spacer(Modifier.height(32.dp))
    }
}
