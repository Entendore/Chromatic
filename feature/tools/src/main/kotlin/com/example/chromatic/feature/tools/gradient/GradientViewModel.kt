package com.example.chromatic.feature.tools.gradient

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import com.example.chromatic.core.color.BlendSpace
import com.example.chromatic.core.color.ColorBlender
import com.example.chromatic.core.color.Rgb
import com.example.chromatic.core.color.fromHexOrNull
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Immutable
data class GradientUiState(
    val fromHex: String = "#1B1B3A",
    val toHex: String = "#F76B8A",
    val from: Rgb? = Rgb.fromHexOrNull("#1B1B3A"),
    val to: Rgb? = Rgb.fromHexOrNull("#F76B8A"),
    val space: BlendSpace = BlendSpace.OKLAB,
    /** Memoized at update time — never recomputed per read (v9 §0a). */
    val stops: List<Rgb> = ColorBlender.ramp(
        Rgb.fromHexOrNull("#1B1B3A")!!, Rgb.fromHexOrNull("#F76B8A")!!, 9, BlendSpace.OKLAB,
    ),
) {
    val isValid: Boolean get() = from != null && to != null
}

@HiltViewModel
class GradientViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(GradientUiState())
    val uiState: StateFlow<GradientUiState> = _uiState.asStateFlow()

    private fun recalculated(state: GradientUiState): GradientUiState = state.copy(
        stops = if (state.from != null && state.to != null) {
            ColorBlender.ramp(state.from!!, state.to!!, 9, state.space)
        } else {
            state.stops // mid-edit: keep the last valid ramp (v12 §4 test contract)
        },
    )

    fun onFromChange(hex: String) = _uiState.update {
        recalculated(it.copy(fromHex = hex, from = Rgb.fromHexOrNull(hex) ?: it.from))
    }

    fun onToChange(hex: String) = _uiState.update {
        recalculated(it.copy(toHex = hex, to = Rgb.fromHexOrNull(hex) ?: it.to))
    }

    fun onSwap() = _uiState.update {
        recalculated(it.copy(fromHex = it.toHex, toHex = it.fromHex, from = it.to, to = it.from))
    }

    fun onSpaceChange(space: BlendSpace) = _uiState.update { recalculated(it.copy(space = space)) }
}
