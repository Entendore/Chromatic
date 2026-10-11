package com.example.chromatic.feature.learn

import app.cash.turbine.test
import com.example.chromatic.core.data.model.Lesson
import com.example.chromatic.core.data.repository.QuizGameType
import com.example.chromatic.core.data.repository.QuizRecord
import com.example.chromatic.core.data.repository.UserSettings
import com.example.chromatic.core.data.time.AppClock
import com.example.chromatic.core.testing.FakeLessonRepository
import com.example.chromatic.core.testing.FakeProgressRepository
import com.example.chromatic.core.testing.FakeSettingsRepository
import com.example.chromatic.core.testing.MainDispatcherRule
import com.example.chromatic.core.testing.RecordingQuizRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LearnViewModelTest {

    @get:Rule val main = MainDispatcherRule()

    private val lesson = Lesson(1, "Wheel", "Foundations", "s", sections = emptyList())
    private val day = 86_400_000L

    @Test fun `progress counts completed lessons`() = runTest {
        val progress = FakeProgressRepository()
        val vm = LearnViewModel(
            FakeLessonRepository(listOf(lesson, lesson.copy(id = 2))), progress,
            RecordingQuizRepository(), FakeSettingsRepository(),
            AppClock { 0 }, object : com.example.chromatic.core.data.time.AppInfo {
                override val firstInstallMs = 0L
                override val canRequestReview = false
            },
        )
        vm.uiState.test {
            assertEquals(0f, awaitItem().progress)
            progress.toggle(1)
            assertEquals(0.5f, awaitItem().progress)
            progress.toggle(2)
            assertEquals(1f, awaitItem().progress)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test fun `no emissions when nothing changes (Turbine, v13 §6)`() = runTest {
        val vm = LearnViewModel(
            FakeLessonRepository(), FakeProgressRepository(),
            RecordingQuizRepository(), FakeSettingsRepository(),
            AppClock { 0 }, object : com.example.chromatic.core.data.time.AppInfo {
                override val firstInstallMs = 0L
                override val canRequestReview = false
            },
        )
        vm.uiState.test {
            awaitItem()
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test fun `streak reflects records relative to the injected clock (v12 §0b)`() = runTest {
        val quiz = RecordingQuizRepository()
        quiz.records.value = listOf(
            QuizRecord(QuizGameType.KNOWLEDGE, 3, 6, 998 * day),
            QuizRecord(QuizGameType.KNOWLEDGE, 4, 6, 999 * day),
            QuizRecord(QuizGameType.KNOWLEDGE, 5, 6, 1000 * day),
        )
        val vm = LearnViewModel(
            FakeLessonRepository(listOf(lesson)), FakeProgressRepository(), quiz,
            FakeSettingsRepository(), AppClock { 1000 * day },
            object : com.example.chromatic.core.data.time.AppInfo {
                override val firstInstallMs = 0L
                override val canRequestReview = false
            },
        )
        vm.uiState.test {
            assertEquals(3, awaitItem().streakDays)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test fun `review flag rises once and clears when marked (v16 §6)`() = runTest {
        val quiz = RecordingQuizRepository()
        quiz.records.value = List(3) { QuizRecord(QuizGameType.KNOWLEDGE, 1, 6, it * day) }
        val progress = FakeProgressRepository()
        progress.toggle(1); progress.toggle(2)
        val settings = FakeSettingsRepository()
        val vm = LearnViewModel(
            FakeLessonRepository(listOf(lesson)), progress, quiz, settings,
            AppClock { 100 * day },
            object : com.example.chromatic.core.data.time.AppInfo {
                override val firstInstallMs = 0L
                override val canRequestReview = true
            },
        )
        vm.uiState.test {
            assertTrue(awaitItem().shouldRequestReview)
            vm.onReviewPromptShown()
            assertEquals(false, awaitItem().shouldRequestReview)
            cancelAndIgnoreRemainingEvents()
        }
        // And the flag was persisted (derived state, honest UDF):
        assertTrue(settings.applied.isNotEmpty())
        assertTrue(settings.applied.last().lastReviewPromptMs != null)
    }

    @Test fun `review never asked without Play (v17 §5)`() = runTest {
        val quiz = RecordingQuizRepository()
        quiz.records.value = List(3) { QuizRecord(QuizGameType.KNOWLEDGE, 1, 6, it * day) }
        val progress = FakeProgressRepository()
        progress.toggle(1); progress.toggle(2)
        val vm = LearnViewModel(
            FakeLessonRepository(listOf(lesson)), progress, quiz, FakeSettingsRepository(),
            AppClock { 100 * day },
            object : com.example.chromatic.core.data.time.AppInfo {
                override val firstInstallMs = 0L
                override val canRequestReview = false
            },
        )
        vm.uiState.test {
            assertFalse(awaitItem().shouldRequestReview)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test fun `toggle reaches the repository`() = runTest {
        val progress = FakeProgressRepository()
        val vm = LearnViewModel(
            FakeLessonRepository(listOf(lesson)), progress, RecordingQuizRepository(),
            FakeSettingsRepository(), AppClock { 0 },
            object : com.example.chromatic.core.data.time.AppInfo {
                override val firstInstallMs = 0L
                override val canRequestReview = false
            },
        )
        vm.onToggleCompleted(7)
        assertEquals(listOf(7L), progress.toggled)
    }
}
