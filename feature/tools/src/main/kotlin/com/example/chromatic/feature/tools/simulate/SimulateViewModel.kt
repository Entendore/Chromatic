package com.example.chromatic.feature.tools.simulate

import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import com.example.chromatic.core.color.ColorVisionSimulator
import com.example.chromatic.core.color.ColorVisionType
import com.example.chromatic.core.color.Rgb
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

private val DemoPalette =
    listOf("#0D47A1", "#1976D2", "#388E3C", "#FBC02D", "#F57C00", "#D32F2F", "#7B1FA2", "#795548")
        .mapNotNull(Rgb::fromHexOrNull)

@Immutable
data class SimulateUiState(
    val type: ColorVisionType = ColorVisionType.DEUTERANOPIA,
    val demoPalette: List<Rgb> = DemoPalette,
    val photoUri: Uri? = null,          // a handle — never pixel data in the VM (v8 §2)
)

@HiltViewModel
class SimulateViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(SimulateUiState())
    val uiState: StateFlow<SimulateUiState> = _uiState.asStateFlow()

    /** Precise Machado (linear RGB) — the reference model, for per-color swatches. */
    fun simulate(rgb: Rgb): Rgb = ColorVisionSimulator.simulate(rgb, _uiState.value.type)

    fun onTypeSelected(type: ColorVisionType) = _uiState.update { it.copy(type = type) }
    fun onImagePicked(uri: Uri) = _uiState.update { it.copy(photoUri = uri) }
}
