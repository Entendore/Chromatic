package com.example.chromatic.feature.quiz

import com.example.chromatic.core.color.QuizOption
import com.example.chromatic.core.color.QuizQuestion
import com.example.chromatic.core.color.QuizText
import com.example.chromatic.core.color.QuestionKind
import com.example.chromatic.core.color.TextKey

/** Static knowledge questions as pure key references (v16 §2) — no Context, no resources at build time. */
object StaticBank {
    val questions: List<QuizQuestion> = listOf(
        static(TextKey.S1_PROMPT, TextKey.S1_A, TextKey.S1_B, TextKey.S1_C, TextKey.S1_D, correctIndex = 2, TextKey.S1_EXPL),
        static(TextKey.S2_PROMPT, TextKey.S2_A, TextKey.S2_B, TextKey.S2_C, TextKey.S2_D, correctIndex = 1, TextKey.S2_EXPL),
        static(TextKey.S3_PROMPT, TextKey.S3_A, TextKey.S3_B, TextKey.S3_C, TextKey.S3_D, correctIndex = 1, TextKey.S3_EXPL),
        static(TextKey.S4_PROMPT, TextKey.S4_A, TextKey.S4_B, TextKey.S4_C, TextKey.S4_D, correctIndex = 0, TextKey.S4_EXPL),
        static(TextKey.S5_PROMPT, TextKey.S5_A, TextKey.S5_B, TextKey.S5_C, TextKey.S5_D, correctIndex = 2, TextKey.S5_EXPL),
        static(TextKey.S6_PROMPT, TextKey.S6_A, TextKey.S6_B, TextKey.S6_C, TextKey.S6_D, correctIndex = 1, TextKey.S6_EXPL),
    )

    private fun static(
        prompt: TextKey,
        a: TextKey, b: TextKey, c: TextKey, d: TextKey,
        correctIndex: Int,
        expl: TextKey,
    ) = QuizQuestion(
        kind = QuestionKind.STATIC,
        prompt = QuizText.Key(prompt),
        options = listOf(
            QuizOption(QuizText.Key(a)), QuizOption(QuizText.Key(b)),
            QuizOption(QuizText.Key(c)), QuizOption(QuizText.Key(d)),
        ),
        correctIndex = correctIndex,
        explanation = QuizText.Key(expl),
    )
}
