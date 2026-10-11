package com.example.chromatic.feature.quiz

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chromatic.core.color.Hsl
import com.example.chromatic.core.color.HueMatch
import com.example.chromatic.core.color.LightnessGame
import com.example.chromatic.core.color.NameGame
import com.example.chromatic.core.color.QuizEngine
import com.example.chromatic.core.color.QuizQuestion
import com.example.chromatic.core.color.Rgb
import com.example.chromatic.core.data.repository.QuizGameType
import com.example.chromatic.core.data.repository.QuizRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.random.Random
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ============================================================ Knowledge
@Immutable
data class KnowledgeState(
    val questions: List<QuizQuestion> = emptyList(),
    val index: Int = 0,
    val selected: Int? = null,
    val score: Int = 0,
    val finished: Boolean = false,
) {
    val current: QuizQuestion? get() = questions.getOrNull(index)
}

@HiltViewModel
class KnowledgeQuizViewModel @Inject constructor(
    private val quizRepository: QuizRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(KnowledgeState())
    val state = _state.asStateFlow()

    init { start() }

    fun start() {
        _state.value = KnowledgeState(
            questions = QuizEngine.generate(count = 6, staticBank = StaticBank.questions),
        )
    }

    fun onSelect(optionIndex: Int) {
        val current = _state.value.current ?: return
        if (_state.value.selected != null) return            // idempotent per question (v12 §4)
        val correct = optionIndex == current.correctIndex
        _state.update { it.copy(selected = optionIndex, score = it.score + if (correct) 1 else 0) }
    }

    fun onNext() {
        val s = _state.value
        if (s.selected == null) return                        // guard: no advance without an answer
        if (s.index + 1 >= s.questions.size) {
            _state.update { it.copy(finished = true) }
            viewModelScope.launch { quizRepository.record(QuizGameType.KNOWLEDGE, s.score, s.questions.size) }
        } else {
            _state.update { it.copy(index = it.index + 1, selected = null) }
        }
    }
}

// ============================================================ Hue match
@Immutable
data class HueRound(val target: Rgb, val guess: Hsl = Hsl(210f, 0.5f, 0.5f), val result: Int? = null)

@Immutable
data class HueMatchState(
    val rounds: List<HueRound> = emptyList(),
    val roundIndex: Int = 0,
    val totalScore: Int = 0,
    val finished: Boolean = false,
) {
    val current: HueRound get() = rounds[roundIndex]
}

@HiltViewModel
class HueMatchViewModel @Inject constructor(
    private val quizRepository: QuizRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HueMatchState(newRounds()))
    val state = _state.asStateFlow()

    private fun newRounds(count: Int = 3) = List(count) {
        HueRound(
            target = Hsl(
                hue = Random.Default.nextFloat() * 360f,
                saturation = 0.4f + Random.Default.nextFloat() * 0.55f,
                lightness = 0.3f + Random.Default.nextFloat() * 0.45f,
            ).toRgb(),
        )
    }

    fun onGuessChange(hsl: Hsl) {
        if (_state.value.current.result != null) return
        _state.update { s ->
            s.copy(rounds = s.rounds.toMutableList().also {
                it[s.roundIndex] = it[s.roundIndex].copy(guess = hsl)
            })
        }
    }

    fun onCheck() {
        val s = _state.value
        val round = s.current
        if (round.result != null) return                      // no double scoring (v12 §4)
        val score = HueMatch.score(round.target, round.guess.toRgb())
        val rounds = s.rounds.toMutableList().also { it[s.roundIndex] = round.copy(result = score) }
        val total = s.totalScore + score
        val isLast = s.roundIndex + 1 >= rounds.size
        _state.update { it.copy(rounds = rounds, totalScore = total, finished = isLast) }
        if (isLast) {
            viewModelScope.launch { quizRepository.record(QuizGameType.HUE_MATCH, total, rounds.size * 100) }
        }
    }

    fun onNext() {
        val s = _state.value
        if (s.finished || s.current.result == null) return
        _state.update { it.copy(roundIndex = it.roundIndex + 1) }
    }

    fun restart() { _state.value = HueMatchState(newRounds()) }
}

// ============================================================ Name it
@Immutable
data class NameColorState(
    val questions: List<QuizQuestion> = emptyList(),
    val index: Int = 0,
    val selected: Int? = null,
    val score: Int = 0,
    val finished: Boolean = false,
) {
    val current: QuizQuestion? get() = questions.getOrNull(index)
}

@HiltViewModel
class NameColorViewModel @Inject constructor(
    private val quizRepository: QuizRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(NameColorState())
    val state = _state.asStateFlow()

    init { restart() }

    fun restart() {
        _state.value = NameColorState(questions = List(5) { NameGame.generate(Random.Default) })
    }

    fun onSelect(optionIndex: Int) {
        val current = _state.value.current ?: return
        if (_state.value.selected != null) return
        val correct = optionIndex == current.correctIndex
        _state.update { it.copy(selected = optionIndex, score = it.score + if (correct) 1 else 0) }
    }

    fun onNext() {
        val s = _state.value
        if (s.selected == null) return
        if (s.index + 1 >= s.questions.size) {
            _state.update { it.copy(finished = true) }
            viewModelScope.launch { quizRepository.record(QuizGameType.NAME_COLOR, s.score, s.questions.size) }
        } else {
            _state.update { it.copy(index = it.index + 1, selected = null) }
        }
    }
}

// ============================================================ Lighter?
@Immutable
data class LightnessState(
    val round: LightnessGame.Round,
    val difficulty: Double = LightnessGame.INITIAL_DIFFICULTY,
    val index: Int = 1,
    val score: Int = 0,
    val lastResult: Boolean? = null,
    val finished: Boolean = false,
)

@HiltViewModel
class LightnessViewModel @Inject constructor(
    private val quizRepository: QuizRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(
        LightnessState(round = LightnessGame.round(LightnessGame.INITIAL_DIFFICULTY, Random.Default)),
    )
    val state = _state.asStateFlow()

    fun onAnswer(choseFirst: Boolean) {
        if (_state.value.lastResult != null) return
        val correct = LightnessGame.isCorrect(_state.value.round, choseFirst)
        _state.update { it.copy(lastResult = correct, score = it.score + if (correct) 20 else 0) }
    }

    fun onNext() {
        val s = _state.value
        val correct = s.lastResult ?: return
        if (s.index >= 5) {
            _state.update { it.copy(finished = true) }
            viewModelScope.launch { quizRepository.record(QuizGameType.LIGHTNESS, s.score, 100) }
        } else {
            val d = LightnessGame.nextDifficulty(correct, s.difficulty)
            _state.update {
                it.copy(
                    round = LightnessGame.round(d, Random.Default),
                    difficulty = d, index = it.index + 1, lastResult = null,
                )
            }
        }
    }

    fun restart() {
        _state.value = LightnessState(
            round = LightnessGame.round(LightnessGame.INITIAL_DIFFICULTY, Random.Default),
        )
    }
}
