package com.example.chromatic.feature.tools

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.chromatic.core.color.ExtractedSwatch
import com.example.chromatic.core.color.Rgb
import com.example.chromatic.core.data.repository.SettingsRepository
import com.example.chromatic.core.testing.FakeSettingsRepository
import com.example.chromatic.core.testing.MainDispatcherRule
import com.example.chromatic.feature.tools.data.PhotoPaletteExtractor
import com.example.chromatic.feature.tools.themestudio.ThemeStudioViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private class FakeExtractor(var result: List<ExtractedSwatch> = emptyList(), var throwOnCall: Boolean = false) :
    PhotoPaletteExtractor {
    var calls = 0
    override suspend fun extract(uri: Uri, clusterCount: Int): List<ExtractedSwatch> {
        calls++
        if (throwOnCall) throw java.io.IOException("provider gone")
        return result
    }
}

@RunWith(AndroidJUnit4::class)
class ThemeStudioViewModelTest {

    @get:Rule val main = MainDispatcherRule()

    private fun vm(
        handle: SavedStateHandle = SavedStateHandle(),
        extractor: FakeExtractor = FakeExtractor(),
        settings: SettingsRepository = FakeSettingsRepository(),
    ) = ThemeStudioViewModel(handle, extractor, settings)

    @Test fun `route arguments seed the initial color (v6 §4)`() {
        val handle = SavedStateHandle(mapOf("seedHue" to 10f, "seedSaturation" to 0.5f, "seedLightness" to 0.6f))
        assertEquals(10f, vm(handle).state.value.seed.hue)
    }

    @Test fun `picked photo populates swatches and clears the flag`() {
        val fake = FakeExtractor(listOf(ExtractedSwatch(Rgb(200, 30, 30), 0.5, "Red")))
        val viewModel = vm(extractor = fake)
        viewModel.onImagePicked(Uri.parse("content://test/palette.png"))
        assertEquals(1, fake.calls)
        assertEquals("Red", viewModel.state.value.extracted.first().name)
        assertFalse(viewModel.state.value.extracting)
    }

    @Test fun `extraction failure clears the flag and empties results (v11 §0c contract)`() {
        val viewModel = vm(extractor = FakeExtractor(throwOnCall = true))
        viewModel.onImagePicked(Uri.parse("content://test/gone.png"))
        assertFalse(viewModel.state.value.extracting)
        assertTrue(viewModel.state.value.extracted.isEmpty())
    }

    @Test fun `seed from swatch adopts the swatch hsl and keeps extraction`() {
        val fake = FakeExtractor(listOf(ExtractedSwatch(Rgb(200, 30, 30), 0.5, "Red")))
        val viewModel = vm(extractor = fake)
        viewModel.onImagePicked(Uri.parse("content://test/x"))
        viewModel.onSeedFromSwatch(Rgb(90, 140, 210))
        assertEquals(Rgb(90, 140, 210).toHsl(), viewModel.state.value.seed)
        assertEquals(1, viewModel.state.value.extracted.size)
    }

    @Test fun `apply persists the seed hex (v18 flagship)`() = runTest {
        val settings = FakeSettingsRepository()
        val viewModel = vm(settings = settings)
        viewModel.onHueChange(123f)
        viewModel.onApplyTheme()
        assertEquals(viewModel.state.value.seed.toRgb().hex, settings.settings.value.appliedThemeSeedHex)
    }
}
