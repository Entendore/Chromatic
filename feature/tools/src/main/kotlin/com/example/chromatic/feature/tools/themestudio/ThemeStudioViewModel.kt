package com.example.chromatic.feature.tools.themestudio

import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chromatic.core.color.ExtractedSwatch
import com.example.chromatic.core.color.Hsl
import com.example.chromatic.core.color.Rgb
import com.example.chromatic.core.color.ThemeBundle
import com.example.chromatic.core.color.ThemeGenerator
import com.example.chromatic.core.data.repository.SettingsRepository
import com.example.chromatic.feature.tools.data.PhotoPaletteExtractor
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.random.Random
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class ThemeStudioState(
    val seed: Hsl,
    val darkPreview: Boolean = false,
    val extracted: List<ExtractedSwatch> = emptyList(),
    val extracting: Boolean = false,
)

@HiltViewModel
class ThemeStudioViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val extractor: PhotoPaletteExtractor,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(
        ThemeStudioState(
            seed = Hsl(
                hue = savedStateHandle["seedHue"] ?: 258f,
                saturation = savedStateHandle["seedSaturation"] ?: 0.72f,
                lightness = savedStateHandle["seedLightness"] ?: 0.52f,
            ),
        )
    )
    val state: StateFlow<ThemeStudioState> = _state.asStateFlow()

    fun onHueChange(value: Float) = _state.update { it.copy(seed = it.seed.copy(hue = value)) }
    fun onSaturationChange(value: Float) = _state.update { it.copy(seed = it.seed.copy(saturation = value)) }
    fun onDarkPreview(enabled: Boolean) = _state.update { it.copy(darkPreview = enabled) }

    fun onRandomColor() {
        val r = Random.Default
        _state.update {
            it.copy(seed = Hsl(r.nextFloat() * 360f, 0.4f + r.nextFloat() * 0.6f, 0.45f + r.nextFloat() * 0.15f))
        }
    }

    /** A revoked content:// grant is environmental, not a bug — degrade + clear flag (v11 §0c). */
    fun onImagePicked(uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(extracting = true) }
            val swatches = try {
                extractor.extract(uri)
            } catch (t: Throwable) {
                emptyList()
            }
            _state.update { it.copy(extracted = swatches, extracting = false) }
        }
    }

    fun onSeedFromSwatch(rgb: Rgb) = _state.update { it.copy(seed = rgb.toHsl()) }

    /** v18 flagship: the generator's verified scheme becomes the running app's theme. */
    fun onApplyTheme() {
        viewModelScope.launch { settingsRepository.applyTheme(_state.value.seed.toRgb()) }
    }
}
