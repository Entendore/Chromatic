package com.example.chromatic.feature.tools

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.chromatic.core.color.PaletteStyle
import com.example.chromatic.core.data.model.SavedPalette
import com.example.chromatic.core.testing.FakePaletteRepository
import com.example.chromatic.core.testing.MainDispatcherRule
import com.example.chromatic.feature.tools.data.PaletteImageSharer
import com.example.chromatic.feature.tools.data.PhotoPaletteExtractor
import com.example.chromatic.feature.tools.palette.PaletteBuilderViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private class FakePhotoPaletteExtractor : PhotoPaletteExtractor {
    var result: List<com.example.chromatic.core.color.ExtractedSwatch> = emptyList()
    var calls = 0
    override suspend fun extract(uri: Uri, clusterCount: Int) = ++calls.let { result }
}

private class FakeSharer : PaletteImageSharer {
    var asked: String? = null
    override suspend fun share(name: String, colors: List<com.example.chromatic.core.color.Rgb>): Uri? {
        asked = name
        return Uri.parse("content://test/palette.png")
    }
}

@RunWith(AndroidJUnit4::class)
class PaletteBuilderViewModelTest {

    @get:Rule val main = MainDispatcherRule()

    private fun palette(id: String) = SavedPalette(id, "P$id", listOf("#000000"), id.toLong() * 10)
    private fun vm(
        repo: FakePaletteRepository,
        extractor: FakePhotoPaletteExtractor = FakePhotoPaletteExtractor(),
    ) = PaletteBuilderViewModel(repo, extractor, FakeSharer())

    @Test fun `rapid deletes then two undos restore both (v12 §0c regression)`() = runTest {
        val repo = FakePaletteRepository(listOf(palette("1"), palette("2")))
        val viewModel = vm(repo)
        viewModel.deletePalette("1")
        viewModel.deletePalette("2")
        assertEquals(listOf("1", "2"), repo.deleted)
        viewModel.undoDelete()
        viewModel.undoDelete()
        assertEquals(setOf("1", "2"), repo.palettes.value.map { it.id }.toSet())
    }

    @Test fun `undo beyond the stack is a no-op`() = runTest {
        val repo = FakePaletteRepository(listOf(palette("1")))
        val viewModel = vm(repo)
        viewModel.deletePalette("1")
        viewModel.undoDelete()
        viewModel.undoDelete()
        assertEquals(listOf("1"), repo.palettes.value.map { it.id })
    }

    @Test fun `delete of an unknown id is a no-op`() = runTest {
        val repo = FakePaletteRepository(listOf(palette("1")))
        vm(repo).deletePalette("ghost")
        assertTrue(repo.deleted.isEmpty())
    }

    @Test fun `save persists the full tonal scale`() = runTest {
        val repo = FakePaletteRepository()
        val viewModel = vm(repo)
        viewModel.onNameChange("Jewel mood")
        viewModel.savePalette()
        val saved = repo.saved.single()
        assertEquals(10, saved.hexColors.size)
        assertEquals("Jewel mood", saved.name)
    }

    @Test fun `style palette saves under the style name`() = runTest {
        val repo = FakePaletteRepository()
        val viewModel = vm(repo)
        viewModel.onStyleGenerated(PaletteStyle.JEWEL)
        viewModel.saveStylePalette("Jewel mood")
        val saved = repo.saved.single()
        assertEquals(5, saved.hexColors.size)
        assertEquals("Jewel mood", saved.name)
    }

    @Test fun `extraction populates and selection adopts the base`() = runTest {
        val extractor = FakePhotoPaletteExtractor()
        extractor.result = listOf(com.example.chromatic.core.color.ExtractedSwatch(
            com.example.chromatic.core.color.Rgb(200, 30, 30), 0.5, "Red"))
        val repo = FakePaletteRepository()
        val viewModel = vm(repo, extractor)
        viewModel.onImagePicked(Uri.parse("content://test/x.png"))
        assertEquals(1, viewModel.uiState.value.extracted.size)
        viewModel.onExtractedColorSelected(com.example.chromatic.core.color.Rgb(200, 30, 30))
        assertEquals(
            com.example.chromatic.core.color.Rgb(200, 30, 30).toHsl(),
            viewModel.uiState.value.baseHsl,
        )
    }

    @Test fun `png share produces a uri then consumes it`() = runTest {
        val repo = FakePaletteRepository(listOf(palette("1")))
        val viewModel = vm(repo)
        viewModel.onSharePng(palette("1"))
        assertEquals("P1", (viewModel.shareUri.value != null).let { viewModel.shareUri.value?.let { "P1" } })
        viewModel.onShareConsumed()
        assertEquals(null, viewModel.shareUri.value)
    }
}
