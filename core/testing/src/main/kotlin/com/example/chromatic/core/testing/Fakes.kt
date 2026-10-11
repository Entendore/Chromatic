package com.example.chromatic.core.testing

import com.example.chromatic.core.color.ColorVisionType
import com.example.chromatic.core.color.Rgb
import com.example.chromatic.core.data.model.Lesson
import com.example.chromatic.core.data.model.SavedPalette
import com.example.chromatic.core.data.repository.LessonRepository
import com.example.chromatic.core.data.repository.PaletteRepository
import com.example.chromatic.core.data.repository.ProgressRepository
import com.example.chromatic.core.data.repository.QuizGameType
import com.example.chromatic.core.data.repository.QuizRecord
import com.example.chromatic.core.data.repository.QuizRepository
import com.example.chromatic.core.data.repository.QuizStats
import com.example.chromatic.core.data.repository.SettingsRepository
import com.example.chromatic.core.data.repository.ThemeMode
import com.example.chromatic.core.data.repository.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeLessonRepository(initial: List<Lesson> = emptyList()) : LessonRepository {
    private val _lessons = MutableStateFlow(initial)
    val current: List<Lesson> get() = _lessons.value
    override val lessons: Flow<List<Lesson>> = _lessons
    override fun findById(id: Long): Flow<Lesson?> = _lessons.map { l -> l.find { it.id == id } }
}

class FakeProgressRepository : ProgressRepository {
    private val _completed = MutableStateFlow<Set<Long>>(emptySet())
    val toggled = mutableListOf<Long>()
    override val completedLessonIds: Flow<Set<Long>> = _completed
    override suspend fun toggle(lessonId: Long) {
        toggled += lessonId
        _completed.value = if (lessonId in _completed.value) {
            _completed.value - lessonId
        } else {
            _completed.value + lessonId
        }
    }
}

class RecordingQuizRepository : QuizRepository {
    val recorded = mutableListOf<QuizGameType>()
    override val stats = MutableStateFlow(QuizStats())
    override val records = MutableStateFlow<List<QuizRecord>>(emptyList())
    override suspend fun record(gameType: QuizGameType, score: Int, maxScore: Int) {
        recorded += gameType
    }
}

class FakePaletteRepository(initial: List<SavedPalette> = emptyList()) : PaletteRepository {
    private val _palettes =
        MutableStateFlow(initial.sortedByDescending { it.createdAtEpochMs })
    val saved = mutableListOf<SavedPalette>()
    val deleted = mutableListOf<String>()
    override val palettes: Flow<List<SavedPalette>> = _palettes
    override suspend fun save(p: SavedPalette) {
        saved += p
        _palettes.value = (_palettes.value.filterNot { it.id == p.id } + p)
            .sortedByDescending { it.createdAtEpochMs }
    }
    override suspend fun delete(id: String) {
        deleted += id
        _palettes.value = _palettes.value.filterNot { it.id == id }
    }
}

class FakeSettingsRepository(initial: UserSettings = UserSettings()) : SettingsRepository {
    private val _settings = MutableStateFlow(initial)
    val applied = mutableListOf<UserSettings>()
    override val settings: Flow<UserSettings> = _settings
    override suspend fun setThemeMode(mode: ThemeMode) {
        _settings.value = _settings.value.copy(themeMode = mode); applied += _settings.value
    }
    override suspend fun setDynamicColor(enabled: Boolean) {
        _settings.value = _settings.value.copy(dynamicColor = enabled); applied += _settings.value
    }
    override suspend fun setCvdPreview(type: ColorVisionType?) {
        _settings.value = _settings.value.copy(cvdPreview = type); applied += _settings.value
    }
    override suspend fun markReviewAsked(nowMs: Long) {
        _settings.value = _settings.value.copy(lastReviewPromptMs = nowMs); applied += _settings.value
    }
    override suspend fun applyTheme(seed: Rgb) {
        _settings.value = _settings.value.copy(appliedThemeSeedHex = seed.hex); applied += _settings.value
    }
    override suspend fun resetTheme() {
        _settings.value = _settings.value.copy(appliedThemeSeedHex = null); applied += _settings.value
    }
    override suspend fun completeOnboarding() {
        _settings.value = _settings.value.copy(onboardingDone = true); applied += _settings.value
    }
}
