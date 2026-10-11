package com.example.chromatic.feature.tools.palette

import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chromatic.core.color.ExtractedSwatch
import com.example.chromatic.core.color.Hsl
import com.example.chromatic.core.color.PaletteFactory
import com.example.chromatic.core.color.PaletteStyle
import com.example.chromatic.core.color.PaletteStyles
import com.example.chromatic.core.color.Rgb
import com.example.chromatic.core.color.toHsv
import com.example.chromatic.core.color.toRgb
import com.example.chromatic.core.data.model.SavedPalette
import com.example.chromatic.core.data.repository.PaletteRepository
import com.example.chromatic.feature.tools.data.PaletteImageSharer
import com.example.chromatic.feature.tools.data.PhotoPaletteExtractor
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow

@Immutable
data class PaletteBuilderUiState(
    val baseHsl: Hsl = Hsl(262f, 0.62f, 0.52f),
    val name: String = "",
    val savedPalettes: List<SavedPalette> = emptyList(),
    val extracted: List<ExtractedSwatch> = emptyList(),
    val styleResult: Pair<PaletteStyle, List<Rgb>>? = null,
)

@HiltViewModel
class PaletteBuilderViewModel @Inject constructor(
    private val paletteRepository: PaletteRepository,
    private val extractor: PhotoPaletteExtractor,
    private val sharer: PaletteImageSharer,
) : ViewModel() {

    private val base = MutableStateFlow(Hsl(262f, 0.62f, 0.52f))
    private val name = MutableStateFlow("")
    private val extracted = MutableStateFlow<List<ExtractedSwatch>>(emptyList())
    private val styleResult = MutableStateFlow<Pair<PaletteStyle, List<Rgb>>?>(null)
    private val _shareUri = MutableStateFlow<Uri?>(null)
    val shareUri: StateFlow<Uri?> = _shareUri.asStateFlow()

    private val undoStack = ArrayDeque<SavedPalette>()

    val uiState: StateFlow<PaletteBuilderUiState> = combine(
        base, name, paletteRepository.palettes, extracted, styleResult,
    ) { hsl, n, saved, ex, style ->
        PaletteBuilderUiState(
            baseHsl = hsl, name = n, savedPalettes = saved,
            extracted = ex, styleResult = style,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PaletteBuilderUiState())

    // Heavy derived data memoized against the base (v6 §0c) — drag frames stay cheap.
    private var toneCacheKey: Hsl? = null
    private var toneCache: List<PaletteFactory.Tone> = emptyList()
    fun tonesFor(baseHsl: Hsl): List<PaletteFactory.Tone> {
        if (toneCacheKey != baseHsl) {
            toneCacheKey = baseHsl
            toneCache = PaletteFactory.tonalScale(baseHsl.toRgb())
        }
        return toneCache
    }

    fun onHueChange(hue: Float) { base.value = base.value.copy(hue = hue) }
    fun onSaturationChange(s: Float) { base.value = base.value.copy(saturation = s) }
    fun onLightnessChange(l: Float) { base.value = base.value.copy(lightness = l) }
    fun onNameChange(value: String) { name.value = value }
    fun onBasePicked(rgb: Rgb) { base.value = rgb.toHsl() }
    fun onExtractedColorSelected(rgb: Rgb) { base.value = rgb.toHsl() }

    fun onRandomBase() {
        val r = kotlin.random.Random.Default
        base.value = Hsl(r.nextFloat() * 360f, 0.35f + r.nextFloat() * 0.6f, 0.35f + r.nextFloat() * 0.4f)
    }

    fun onImagePicked(uri: Uri) {
        viewModelScope.launch {
            extracted.value = extractor.extract(uri)
        }
    }

    fun onStyleGenerated(style: PaletteStyle) {
        styleResult.value = style to PaletteStyles.palette(style)
    }

    fun savePalette() {
        val state = uiState.value
        viewModelScope.launch {
            paletteRepository.save(
                SavedPalette(
                    id = java.util.UUID.randomUUID().toString(),
                    name = state.name.ifBlank { "Palette ${state.savedPalettes.size + 1}" },
                    hexColors = tonesFor(state.baseHsl).map { it.rgb.hex },
                    createdAtEpochMs = System.currentTimeMillis(),
                )
            )
        }
        name.value = ""
    }

    fun saveStylePalette(displayName: String) {
        val (_, colors) = styleResult.value ?: return
        viewModelScope.launch {
            paletteRepository.save(
                SavedPalette(
                    id = java.util.UUID.randomUUID().toString(),
                    name = displayName,
                    hexColors = colors.map(Rgb::hex),
                    createdAtEpochMs = System.currentTimeMillis(),
                )
            )
        }
    }

    /** Bounded undo stack — rapid deletes never lose victims (v12 §0c). */
    fun deletePalette(id: String) {
        val victim = uiState.value.savedPalettes.firstOrNull { it.id == id } ?: return
        undoStack.addLast(victim)
        if (undoStack.size > MAX_UNDO) undoStack.removeFirst()
        viewModelScope.launch { paletteRepository.delete(id) }
    }

    fun undoDelete() {
        val victim = undoStack.removeLastOrNull() ?: return
        viewModelScope.launch { paletteRepository.save(victim) }
    }

    /** Suspend work owned by viewModelScope; UI only observes the URI (v9 §3). */
    fun onSharePng(palette: SavedPalette) {
        viewModelScope.launch {
            val colors = palette.hexColors.mapNotNull(Rgb::fromHexOrNull)
            _shareUri.value = sharer.share(palette.name, colors)
        }
    }

    fun onShareConsumed() { _shareUri.value = null }

    private companion object { const val MAX_UNDO = 5 }
}
