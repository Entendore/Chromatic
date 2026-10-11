package com.example.chromatic.feature.tools.harmony

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import com.example.chromatic.core.color.ColorHarmony
import com.example.chromatic.core.color.ColorVisionType
import com.example.chromatic.core.color.CvdSafePalette
import com.example.chromatic.core.color.HarmonyMode
import com.example.chromatic.core.color.Hsv
import com.example.chromatic.core.color.Rgb
import com.example.chromatic.core.color.toRgb
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

@Immutable
data class HarmonyUiState(
    val baseHsv: Hsv = DefaultBase,
    val harmony: ColorHarmony = ColorHarmony.COMPLEMENTARY,
    val mode: HarmonyMode = HarmonyMode.TRADITIONAL,
    val palette: PersistentList<Rgb> =
        ColorHarmony.COMPLEMENTARY.generate(DefaultBase).map(Hsv::toRgb).toPersistentList(),
) {
    private companion object {
        val DefaultBase = Hsv(hue = 217f, saturation = 0.68f, value = 0.92f)
    }
}

@HiltViewModel
class HarmonyViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(HarmonyUiState())
    val uiState: StateFlow<HarmonyUiState> = _uiState.asStateFlow()

    private val _cvdResult = MutableStateFlow<CvdSafePalette.Result?>(null)
    val cvdResult: StateFlow<CvdSafePalette.Result?> = _cvdResult.asStateFlow()

    /** Every palette mutation invalidates the CVD analysis — ONE gate (v6 §0b), test-locked. */
    private fun mutate(transform: (HarmonyUiState) -> HarmonyUiState) {
        _cvdResult.value = null
        _uiState.update(transform)
    }

    /** Conflated drag input (v8 §3): at most one regeneration in flight. */
    fun onPaletteInput(hue: Float, saturation: Float, value: Float) =
        mutate { it.copy(baseHsv = Hsv(hue, saturation, value.coerceIn(0.1f, 1f))).regenerated() }

    fun onHarmonySelected(harmony: ColorHarmony) = mutate { it.copy(harmony = harmony).regenerated() }

    fun onModeSelected(mode: HarmonyMode) = mutate { it.copy(mode = mode).regenerated() }

    fun onRandomColor() {
        val r = Random.Default
        mutate {
            it.copy(
                baseHsv = Hsv(
                    hue = r.nextFloat() * 360f,
                    saturation = 0.35f + r.nextFloat() * 0.65f,
                    value = 0.75f + r.nextFloat() * 0.25f,
                ),
            ).regenerated()
        }
    }

    fun onToggleCvdSafe() {
        _cvdResult.value = if (_cvdResult.value != null) null
        else CvdSafePalette.makeDistinguishable(_uiState.value.palette, ColorVisionType.DEUTERANOPIA)
    }

    private fun HarmonyUiState.regenerated() =
        copy(palette = harmony.generate(baseHsv, mode).map(Hsv::toRgb).toPersistentList())
}
