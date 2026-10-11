package com.example.chromatic.core.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.chromatic.core.color.ColorVisionType
import com.example.chromatic.core.color.Rgb
import com.example.chromatic.core.data.repository.DataStoreProgressRepository
import com.example.chromatic.core.data.repository.DataStoreSettingsRepository
import com.example.chromatic.core.data.repository.ThemeMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class DataStoreRepositoriesTest {

    @get:Rule val tmp = TemporaryFolder()

    private fun newStore(name: String) = PreferenceDataStoreFactory.create(
        scope = CoroutineScope(UnconfinedTestDispatcher() + SupervisorJob()),
        produceFile = { File(tmp.root, name) },
    )

    @Test fun `rapid toggles never lose an update (v12 race regression)`() = runTest {
        val repo = DataStoreProgressRepository(newStore("progress.preferences_pb"))
        repo.toggle(1L)
        repo.toggle(1L)
        repo.toggle(1L)
        assertEquals(setOf(1L), repo.completedLessonIds.first())
    }

    @Test fun `settings round-trip including cvd null removal`() = runTest {
        val repo = DataStoreSettingsRepository(newStore("s1.preferences_pb"))
        repo.setThemeMode(ThemeMode.DARK)
        repo.setDynamicColor(false)
        repo.setCvdPreview(ColorVisionType.TRITANOPIA)
        with(repo.settings.first()) {
            assertEquals(ThemeMode.DARK, themeMode)
            assertFalse(dynamicColor)
            assertEquals(ColorVisionType.TRITANOPIA, cvdPreview)
        }
        repo.setCvdPreview(null)
        assertNull(repo.settings.first().cvdPreview)
    }

    @Test fun `apply then reset returns to null seed (v18)`() = runTest {
        val repo = DataStoreSettingsRepository(newStore("s2.preferences_pb"))
        repo.applyTheme(Rgb(75, 90, 197))
        assertEquals(Rgb(75, 90, 197).hex, repo.settings.first().appliedThemeSeedHex)
        repo.resetTheme()
        assertNull(repo.settings.first().appliedThemeSeedHex)
    }

    @Test fun `onboarding completes and persists (v18)`() = runTest {
        val repo = DataStoreSettingsRepository(newStore("s3.preferences_pb"))
        assertFalse(repo.settings.first().onboardingDone)
        repo.completeOnboarding()
        assertTrue(repo.settings.first().onboardingDone)
    }

    @Test fun `corrupted enum values degrade to defaults without crashing (v12 §5)`() = runTest {
        val ds = newStore("s4.preferences_pb")
        ds.edit { it[stringPreferencesKey("theme_mode")] = "SEPIA" }
        assertEquals(ThemeMode.SYSTEM, DataStoreSettingsRepository(ds).settings.first().themeMode)
    }
}
