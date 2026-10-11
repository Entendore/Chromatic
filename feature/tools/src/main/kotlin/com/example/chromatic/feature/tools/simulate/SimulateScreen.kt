package com.example.chromatic.feature.tools.simulate

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.chromatic.core.color.ColorVisionSimulator
import com.example.chromatic.core.designsystem.components.SectionTitle
import com.example.chromatic.core.designsystem.components.label
import com.example.chromatic.core.designsystem.components.rememberLocalPhoto
import com.example.chromatic.core.designsystem.components.toComposeColor

@Composable
fun SimulateScreen(viewModel: SimulateViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let(viewModel::onImagePicked)
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Text(
            stringResource(R.string.simulate_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(vertical = 16.dp),
        )
        Text(
            stringResource(R.string.simulate_intro),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(ColorVisionType.entries) { type ->
                FilterChip(
                    selected = uiState.type == type,
                    onClick = { viewModel.onTypeSelected(type) },
                    label = { Text(type.label()) },
                )
            }
        }

        SectionTitle(stringResource(R.string.simulate_reference))
        SwatchStrip(stringResource(R.string.original), uiState.demoPalette)
        Spacer(Modifier.height(6.dp))
        SwatchStrip(
            stringResource(R.string.seen_with, uiState.type.label()),
            uiState.demoPalette.map { viewModel.simulate(it) },
        )

        SectionTitle(stringResource(R.string.try_photo))
        Button(onClick = {
            pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }) {
            Icon(Icons.Filled.Image, contentDescription = null, Modifier.size(18.dp))
            Spacer(Modifier.size(8.dp))
            Text(
                if (uiState.photoUri == null) stringResource(R.string.choose_photo)
                else stringResource(R.string.choose_another_photo)
            )
        }
        uiState.photoUri?.let { uri ->
            Spacer(Modifier.height(12.dp))
            val photo = rememberLocalPhoto(uri)
            val matrix = ColorVisionSimulator.srgbMatrix(uiState.type)
                ?.let { ColorFilter.colorMatrix(ColorMatrix(it)) }
            photo?.let { bmp ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Image(
                        bitmap = bmp,
                        contentDescription = stringResource(R.string.photo_original),
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(16.dp)),
                    )
                    Image(
                        bitmap = bmp,
                        contentDescription = stringResource(R.string.photo_simulated, uiState.type.label()),
                        colorFilter = matrix,
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(16.dp)),
                    )
                }
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun SwatchStrip(label: String, colors: List<com.example.chromatic.core.color.Rgb>) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
            colors.forEach { rgb ->
                Box(
                    Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(rgb.toComposeColor())
                )
            }
        }
    }
}
