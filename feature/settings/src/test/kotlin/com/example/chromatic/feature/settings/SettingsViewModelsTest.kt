package com.example.chromatic.feature.settings

import com.example.chromatic.core.color.ColorVisionType
import com.example.chromatic.core.data.repository.ThemeMode
import com.example.chromatic.core.data.time.AppClock
import com.example.chromatic.core.testing.FakeSettingsRepository
import com.example.chromatic.core.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

    @get:Rule val main = MainDispatcherRule()

    private val buildInfo = com.example.chromatic.core.data.BuildInfo("1.0.0", 1, true)

    @Test fun `theme mode reaches the repository`() = runTest {
        val repo = FakeSettingsRepository()
        val vm = SettingsViewModel(repo, buildInfo, AppClock { 0 })
        vm.onThemeModeSelected(ThemeMode.DARK)
        assertEquals(ThemeMode.DARK, repo.settings.value.themeMode)
    }

    @Test fun `cvd preview round-trips including null`() = runTest {
        val repo = FakeSettingsRepository()
        val vm = SettingsViewModel(repo, buildInfo, AppClock { 0 })
        vm.onCvdPreviewSelected(ColorVisionType.TRITANOPIA)
        assertEquals(ColorVisionType.TRITANOPIA, repo.settings.value.cvdPreview)
        vm.onCvdPreviewSelected(null)
        assertNull(repo.settings.value.cvdPreview)
    }

    @Test fun `reset theme clears the applied seed`() = runTest {
        val repo = FakeSettingsRepository()
        repo.applyTheme(com.example.chromatic.core.color.Rgb(75, 90, 197))
        val vm = SettingsViewModel(repo, buildInfo, AppClock { 0 })
        vm.onResetTheme()
        assertNull(repo.settings.value.appliedThemeSeedHex)
    }
}

class CrashReportsViewModelTest {

    @get:Rule val main = MainDispatcherRule()

    private class RecordingStore : com.example.chromatic.core.data.crash.CrashStore {
        var current = mutableListOf<com.example.chromatic.core.data.crash.CrashReport>()
        var cleared = false
        override fun recordSync(report: com.example.chromatic.core.data.crash.CrashReport) { current += report }
        override fun reports() = current.toList()
        override fun clear() { cleared = true; current.clear() }
    }

    private fun report(at: Long) = com.example.chromatic.core.data.crash.CrashReport(
        at, "1.0.0", 1, "main", "java.lang.Exception", "trace", "14", "Pixel 7",
    )

    @Test fun `refresh picks up reports that arrived after construction`() {
        val store = RecordingStore()
        val vm = CrashReportsViewModel(store)
        assertEquals(0, vm.reports.value.size)         // empty at construction
        store.recordSync(report(1_000))
        vm.refresh()
        assertEquals(1, vm.reports.value.size)         // visible after refresh (v18 §0b contract)
    }

    @Test fun `clear empties the flow and the store`() {
        val store = RecordingStore()
        store.recordSync(report(1_000))
        val vm = CrashReportsViewModel(store)
        vm.clear()
        assertTrue(store.cleared)
        assertEquals(0, vm.reports.value.size)
    }

    @Test fun `exportText carries version line and trace`() {
        val store = RecordingStore()
        store.recordSync(report(1_000))
        val vm = CrashReportsViewModel(store)
        val text = vm.exportText()!!
        assertTrue(text.contains("app 1.0.0 (1)"))
        assertTrue(text.contains("at App"))
    }

    @Test fun `exportText is null when empty`() {
        assertNull(CrashReportsViewModel(RecordingStore()).exportText())
    }
}
