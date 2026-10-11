package com.example.chromatic.feature.tools.contrast

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import com.example.chromatic.core.color.ContrastReport
import com.example.chromatic.core.color.Rgb
import com.example.chromatic.core.color.WcagContrast
import com.example.chromatic.core.color.fromHexOrNull
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Immutable
data class ContrastUiState(
    val foregroundHex: String = "#1B1B21",
    val backgroundHex: String = "#FBF8FF",
    val foreground: Rgb? = Rgb.fromHexOrNull("#1B1B21"),
    val background: Rgb? = Rgb.fromHexOrNull("#FBF8FF"),
    val lastFixHex: String? = null,
) {
    val isValid: Boolean get() = foreground != null && background != null
}

@HiltViewModel
class ContrastViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ContrastUiState())
    val uiState: StateFlow<ContrastUiState> = _uiState.asStateFlow()

    fun onForegroundHexChange(hex: String) = _uiState.update {
        it.copy(foregroundHex = hex, foreground = Rgb.fromHexOrNull(hex) ?: it.foreground, lastFixHex = null)
    }

    fun onBackgroundHexChange(hex: String) = _uiState.update {
        it.copy(backgroundHex = hex, background = Rgb.fromHexOrNull(hex) ?: it.background, lastFixHex = null)
    }

    fun onSwap() = _uiState.update {
        it.copy(
            foregroundHex = it.backgroundHex, backgroundHex = it.foregroundHex,
            foreground = it.background, background = it.foreground, lastFixHex = null,
        )
    }

    fun onPickForeground(rgb: Rgb) = _uiState.update {
        it.copy(foregroundHex = rgb.hex, foreground = rgb, lastFixHex = null)
    }

    fun onPickBackground(rgb: Rgb) = _uiState.update {
        it.copy(backgroundHex = rgb.hex, background = rgb, lastFixHex = null)
    }

    /** adjustedForeground is non-null BY PROOF (v12 §0a) — no impossible branch exists. */
    fun onFixForeground() {
        val state = _uiState.value
        val fg = state.foreground ?: return
        val bg = state.background ?: return
        val fixed = WcagContrast.adjustedForeground(fg, bg, WcagContrast.AA_NORMAL)
        _uiState.update { it.copy(foreground = fixed, foregroundHex = fixed.hex, lastFixHex = fixed.hex) }
    }
}
