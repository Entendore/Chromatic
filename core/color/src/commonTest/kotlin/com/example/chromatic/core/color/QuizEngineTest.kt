package com.example.chromatic.core.color

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.random.Random

class QuizEngineTest {
    @Test fun `generation is deterministic for a fixed seed`() =
        assertEquals(QuizEngine.generate(6, Random(1)), QuizEngine.generate(6, Random(1)))

    @Test fun `every question is well formed`() {
        QuizEngine.generate(6, Random(3)).forEach { q ->
            assertTrue(q.options.size >= 2)
            assertTrue(q.correctIndex in q.options.indices)
        }
    }

    @Test fun `contrast questions guarantee exactly one AA pass (semantic filter, v16)`() {
        repeat(20) { seed ->
            QuizEngine.generate(6, Random(seed))
                .filter { it.kind == QuestionKind.CONTRAST }
                .forEach { q ->
                    val passing = q.options.count { option ->
                        val bg = option.swatches[0]; val fg = option.swatches[1]
                        WcagContrast.contrastRatio(fg, bg) >= WcagContrast.AA_NORMAL
                    }
                    assertEquals(1, passing)
                }
        }
    }

    @Test fun `static bank participates in generation`() {
        val static = QuizQuestion(
            kind = QuestionKind.STATIC, prompt = QuizText.Key(TextKey.S1_PROMPT),
            options = listOf(QuizOption(QuizText.Key(TextKey.S1_A)), QuizOption(QuizText.Key(TextKey.S1_B))),
            correctIndex = 0, explanation = QuizText.Key(TextKey.S1_EXPL),
        )
        val qs = QuizEngine.generate(6, Random(5), staticBank = listOf(static))
        assertTrue(qs.any { it.kind == QuestionKind.STATIC })
    }
}

class NameGameTest {
    @Test fun `every question names its own swatch correctly`() {
        repeat(25) { seed ->
            val q = NameGame.generate(Random(seed))
            assertEquals(4, q.options.size)
            assertTrue(q.correctIndex in q.options.indices)
            val correct = (q.options[q.correctIndex].text as QuizText.Raw).value
            assertEquals(1, q.options.count { (it.text as QuizText.Raw).value == correct })
        }
    }

    @Test fun `same seed yields an identical question (double-shuffle regression, v7)`() =
        assertEquals(NameGame.generate(Random(11)), NameGame.generate(Random(11)))
}

class LightnessGameTest {
    @Test fun `the lighter side is always lighter in oklab`() {
        repeat(30) { seed ->
            val round = LightnessGame.round(0.05, Random(seed))
            val lFirst = round.first.toOkLab().l
            val lSecond = round.second.toOkLab().l
            assertEquals(round.lighterIsFirst, lFirst > lSecond)
        }
    }

    @Test fun `difficulty bounds are respected`() {
        var d = LightnessGame.INITIAL_DIFFICULTY
        repeat(20) { d = LightnessGame.nextDifficulty(true, d) }
        assertEquals(LightnessGame.MIN, d, 1e-9)
        repeat(20) { d = LightnessGame.nextDifficulty(false, d) }
        assertEquals(LightnessGame.MAX, d, 1e-9)
    }

    @Test fun `a hit makes the next gap smaller`() {
        val d = LightnessGame.INITIAL_DIFFICULTY
        assertTrue(LightnessGame.nextDifficulty(true, d) < d)
        assertTrue(LightnessGame.nextDifficulty(false, d) > d)
    }
}

class HueMatchTest {
    @Test fun `perfect match scores 100`() =
        assertEquals(100, HueMatch.score(Rgb(120, 120, 120), Rgb(120, 120, 120)))

    @Test fun `distant colors score poorly (v3 correction)`() =
        assertTrue(HueMatch.score(Rgb.BLACK, Rgb.WHITE) < 40)
}
