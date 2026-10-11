package com.example.chromatic.feature.quiz

import com.example.chromatic.core.color.Hsl
import com.example.chromatic.core.color.LightnessGame
import com.example.chromatic.core.data.repository.QuizGameType
import com.example.chromatic.core.testing.MainDispatcherRule
import com.example.chromatic.core.testing.RecordingQuizRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class KnowledgeQuizViewModelTest {

    @get:Rule val main = MainDispatcherRule()

    @Test fun `init generates six questions`() = runTest {
        assertEquals(6, KnowledgeQuizViewModel(RecordingQuizRepository()).state.value.questions.size)
    }

    @Test fun `static bank participates in every run`() = runTest {
        val vm = KnowledgeQuizViewModel(RecordingQuizRepository())
        assertTrue(vm.state.value.questions.any { it.kind == com.example.chromatic.core.color.QuestionKind.STATIC })
    }

    @Test fun `selecting is idempotent per question`() = runTest {
        val vm = KnowledgeQuizViewModel(RecordingQuizRepository())
        vm.onSelect(vm.state.value.current!!.correctIndex)
        val score = vm.state.value.score
        vm.onSelect((vm.state.value.current!!.correctIndex + 1) % 4)
        assertEquals(score, vm.state.value.score)
    }

    @Test fun `next without selecting does nothing`() = runTest {
        val vm = KnowledgeQuizViewModel(RecordingQuizRepository())
        vm.onNext()
        assertEquals(0, vm.state.value.index)
    }

    @Test fun `perfect run records once with full score`() = runTest {
        val repo = RecordingQuizRepository()
        val vm = KnowledgeQuizViewModel(repo)
        repeat(6) {
            vm.onSelect(vm.state.value.current!!.correctIndex)
            vm.onNext()
        }
        assertTrue(vm.state.value.finished)
        assertEquals(6, vm.state.value.score)
        assertEquals(listOf(QuizGameType.KNOWLEDGE), repo.recorded)
    }
}

class HueMatchViewModelTest {

    @get:Rule val main = MainDispatcherRule()

    @Test fun `finishing all rounds records the score exactly once`() = runTest {
        val repo = RecordingQuizRepository()
        val vm = HueMatchViewModel(repo)
        repeat(3) {
            vm.onGuessChange(Hsl(180f, 0.5f, 0.5f))
            vm.onCheck()
            if (!vm.state.value.finished) vm.onNext()
        }
        assertTrue(vm.state.value.finished)
        assertEquals(listOf(QuizGameType.HUE_MATCH), repo.recorded)
    }

    @Test fun `checking twice does not double-score`() = runTest {
        val vm = HueMatchViewModel(RecordingQuizRepository())
        vm.onGuessChange(Hsl(0f, 1f, 1f))
        vm.onCheck()
        val score = vm.state.value.totalScore
        vm.onCheck()
        assertEquals(score, vm.state.value.totalScore)
    }
}

class LightnessViewModelTest {

    @get:Rule val main = MainDispatcherRule()

    private fun LightnessViewModel.playCorrectly() {
        val chooseFirst = LightnessGame.isCorrect(state.value.round, choseFirst = true)
        onAnswer(chooseFirst)
        onNext()
    }

    @Test fun `a perfect run scores 100 and records exactly once`() = runTest {
        val repo = RecordingQuizRepository()
        val vm = LightnessViewModel(repo)
        repeat(5) { vm.playCorrectly() }
        assertTrue(vm.state.value.finished)
        assertEquals(100, vm.state.value.score)
        assertEquals(listOf(QuizGameType.LIGHTNESS), repo.recorded)
    }

    @Test fun `double answering a round is ignored`() = runTest {
        val vm = LightnessViewModel(RecordingQuizRepository())
        val round = vm.state.value.round
        vm.onAnswer(LightnessGame.isCorrect(round, choseFirst = true))
        val score = vm.state.value.score
        vm.onAnswer(!LightnessGame.isCorrect(round, choseFirst = true))
        assertEquals(score, vm.state.value.score)
    }

    @Test fun `difficulty follows the pure adaptation curve`() = runTest {
        val vm = LightnessViewModel(RecordingQuizRepository())
        var previous = vm.state.value.difficulty
        repeat(3) {
            vm.playCorrectly()
            val expected = LightnessGame.nextDifficulty(true, previous)
            assertEquals(expected, vm.state.value.difficulty, 1e-9)
            previous = expected
        }
    }

    @Test fun `restart resets run state`() = runTest {
        val vm = LightnessViewModel(RecordingQuizRepository())
        vm.playCorrectly()
        vm.restart()
        assertEquals(1, vm.state.value.index)
        assertEquals(0, vm.state.value.score)
        assertEquals(null, vm.state.value.lastResult)
    }
}

class NameColorViewModelTest {

    @get:Rule val main = MainDispatcherRule()

    @Test fun `init generates five questions`() = runTest {
        assertEquals(5, NameColorViewModel(RecordingQuizRepository()).state.value.questions.size)
    }

    @Test fun `perfect run records once`() = runTest {
        val repo = RecordingQuizRepository()
        val vm = NameColorViewModel(repo)
        repeat(5) {
            vm.onSelect(vm.state.value.current!!.correctIndex)
            vm.onNext()
        }
        assertTrue(vm.state.value.finished)
        assertEquals(5, vm.state.value.score)
        assertEquals(listOf(QuizGameType.NAME_COLOR), repo.recorded)
    }
}
