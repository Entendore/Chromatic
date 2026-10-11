package com.example.chromatic.feature.learn

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chromatic.core.data.achievement.Achievement
import com.example.chromatic.core.data.achievement.Achievements
import com.example.chromatic.core.data.model.Lesson
import com.example.chromatic.core.data.repository.LessonRepository
import com.example.chromatic.core.data.repository.ProgressRepository
import com.example.chromatic.core.data.repository.QuizRepository
import com.example.chromatic.core.data.repository.SettingsRepository
import com.example.chromatic.core.data.review.ReviewPolicy
import com.example.chromatic.core.data.time.AppClock
import com.example.chromatic.core.data.time.AppInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Immutable
data class LearnUiState(
    val lessons: List<Lesson> = emptyList(),
    val completedIds: Set<Long> = emptySet(),
    val streakDays: Int = 0,
    val achievements: List<Achievement> = emptyList(),
    val quizzesPlayed: Int = 0,
    val shouldRequestReview: Boolean = false,
) {
    val progress: Float
        get() = if (lessons.isEmpty()) 0f
        else completedIds.count { id -> lessons.any { it.id == id } } / lessons.size.toFloat()
}

@HiltViewModel
class LearnViewModel @Inject constructor(
    lessonRepository: LessonRepository,
    private val progressRepository: ProgressRepository,
    quizRepository: QuizRepository,
    private val settingsRepository: SettingsRepository,
    private val clock: AppClock,
    private val appInfo: AppInfo,
) : ViewModel() {

    val uiState: StateFlow<LearnUiState> = combine(
        lessonRepository.lessons,
        progressRepository.completedLessonIds,
        quizRepository.records,
        settingsRepository.settings,
    ) { lessons, done, records, settings ->
        val now = clock.nowMs()
        val streak = Achievements.currentStreakDays(records, now)
        val completed = done.count { id -> lessons.any { it.id == id } }
        LearnUiState(
            lessons = lessons,
            completedIds = done,
            streakDays = streak,
            achievements = Achievements.evaluate(records, now),
            quizzesPlayed = records.size,
            shouldRequestReview = appInfo.canRequestReview && ReviewPolicy.shouldAsk(
                ReviewPolicy.Inputs(
                    streakDays = streak,
                    completedLessons = completed,
                    nowMs = now,
                    lastAskedMs = settings.lastReviewPromptMs,
                    firstInstallMs = appInfo.firstInstallMs,
                    canRequestReview = appInfo.canRequestReview,
                )
            ),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LearnUiState())

    fun onToggleCompleted(lessonId: Long) {
        viewModelScope.launch { progressRepository.toggle(lessonId) }
    }

    /** Mark on LAUNCH, not completion — Play gives no completion signal (v16 §3).
     *  The flow re-emits → the flag clears itself (derived state, never stored boolean). */
    fun onReviewPromptShown() {
        viewModelScope.launch { settingsRepository.markReviewAsked(clock.nowMs()) }
    }
}
