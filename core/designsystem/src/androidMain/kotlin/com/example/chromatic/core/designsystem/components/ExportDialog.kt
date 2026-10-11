package com.example.chromatic.core.designsystem.components

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.example.chromatic.core.color.ExportFormat
import com.example.chromatic.core.color.PaletteExporter
import com.example.chromatic.core.color.Rgb

/** Export formats + native share. PNG share is delegated (suspend work lives in a VM — v9 §3). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportDialog(
    paletteName: String,
    colors: List<Rgb>,
    onDismiss: () -> Unit,
    onShowSnackbar: (String) -> Unit,
    copiedMessage: String = "Copied as",
    onSharePng: (() -> Unit)? = null,
) {
    var format by remember { mutableStateOf(ExportFormat.CSS) }
    val content = remember(paletteName, colors, format) {
        PaletteExporter.export(paletteName, colors, format)
    }
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Export \"$paletteName\"", style = MaterialTheme.typography.titleMedium)
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                ExportFormat.entries.forEach { f ->
                    SegmentedButton(
                        selected = format == f,
                        onClick = { format = f },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = f.ordinal, count = ExportFormat.entries.size,
                        ),
                    ) { Text(f.label()) }
                }
            }
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    content,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .heightIn(max = 220.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(12.dp),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(content))
                        onShowSnackbar("$copiedMessage ${format.label()}")
                    },
                    modifier = Modifier.weight(1f),
                ) { Text("Copy") }
                Button(
                    onClick = {
                        val send = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, content)
                            putExtra(Intent.EXTRA_TITLE, "$paletteName.${format.fileExtension}")
                        }
                        context.startActivity(Intent.createChooser(send, "Share palette"))
                    },
                    modifier = Modifier.weight(1f),
                ) { Text("Share") }
            }
            if (onSharePng != null) {
                FilledTonalButton(
                    onClick = onSharePng,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Share as PNG") }
                Spacer(Modifier.padding(bottom = 4.dp))
            }
        }
    }
}
