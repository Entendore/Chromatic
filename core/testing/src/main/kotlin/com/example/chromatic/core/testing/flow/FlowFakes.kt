package com.example.chromatic.core.testing.flow

import android.app.Application
import com.example.chromatic.core.data.crash.CrashStore
import com.example.chromatic.core.data.crash.FileCrashStore
import com.example.chromatic.core.data.di.RepositoryModule
import com.example.chromatic.core.data.model.Lesson
import com.example.chromatic.core.data.model.Practice
import com.example.chromatic.core.data.model.SavedPalette
import com.example.chromatic.core.data.model.Section
import com.example.chromatic.core.data.repository.LessonRepository
import com.example.chromatic.core.data.repository.PaletteRepository
import com.example.chromatic.core.data.repository.ProgressRepository
import com.example.chromatic.core.data.repository.QuizRepository
import com.example.chromatic.core.data.repository.SettingsRepository
import com.example.chromatic.core.data.repository.UserSettings
import com.example.chromatic.core.data.time.AppClock
import com.example.chromatic.core.testing.FakeLessonRepository
import com.example.chromatic.core.testing.FakePaletteRepository
import com.example.chromatic.core.testing.FakeProgressRepository
import com.example.chromatic.core.testing.FakeSettingsRepository
import com.example.chromatic.core.testing.RecordingQuizRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import java.io.File
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

// (deliberate duplicate import removed by ktlint — kept explicit for script clarity)
@Suppress("unused")
private val unusedImportsGuard: Any = Unit

/** One hermetic graph shared by JVM flow tests AND device androidTest (v13 §2). */
class FlowFakes {
    val lessons = FakeLessonRepository(FLOW_LESSONS)
    val progress = FakeProgressRepository()
    val quiz = RecordingQuizRepository()
    val palettes = FakePaletteRepository(
        listOf(SavedPalette("flow-1", "Sample Palette", listOf("#000000"), 1L)),
    )
    val settings = FakeSettingsRepository(UserSettings(onboardingDone = true))
    val crashStore = FileCrashStore(tempReportsDir()) { 1_000L }

    companion object {
        val FLOW_LESSONS = listOf(
            Lesson(
                1, "The Color Wheel", "Foundations", "Why hues live on a circle.",
                sections = listOf(Section("From Newton to your screen", "Newton bent light through a prism.")),
            ),
            Lesson(
                6, "Contrast & Accessibility", "Application", "WCAG in numbers.",
                sections = listOf(Section("How contrast is measured", "Relative luminance and the ratio.")),
                practices = listOf(Practice.CONTRAST, Practice.GRID),
            ),
        )

        private fun tempReportsDir(): File =
            File(System.getProperty("java.io.tmpdir"), "chromatic-flow-crashes-${System.nanoTime()}")
                .apply { mkdirs() }
    }
}

@Module
@TestInstallIn(components = [SingletonComponent::class])
object FlowTestBindings {
    @Provides @Singleton fun fakes(): FlowFakes = FlowFakes()
    @Provides @Singleton fun lessons(f: FlowFakes): LessonRepository = f.lessons
    @Provides @Singleton fun progress(f: FlowFakes): ProgressRepository = f.progress
    @Provides @Singleton fun quiz(f: FlowFakes): QuizRepository = f.quiz
    @Provides @Singleton fun palettes(f: FlowFakes): PaletteRepository = f.palettes
    @Provides @Singleton fun settings(f: FlowFakes): SettingsRepository = f.settings
    @Provides @Singleton fun crashStore(f: FlowFakes): CrashStore = f.crashStore
    @Provides @Singleton fun clock(): AppClock = AppClock { 1_000L }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface FlowFakesEntryPoint {
    fun fakes(): FlowFakes
}

fun Application.fakes(): FlowFakes =
    EntryPointAccessors.fromApplication(this, FlowFakesEntryPoint::class.java).fakes()
