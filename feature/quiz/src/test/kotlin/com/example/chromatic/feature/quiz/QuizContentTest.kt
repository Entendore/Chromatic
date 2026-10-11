package com.example.chromatic.feature.quiz

import android.content.Context
import android.content.res.Configuration
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.chromatic.core.color.NameGame
import com.example.chromatic.core.color.QuizEngine
import com.example.chromatic.core.color.QuizText
import com.example.chromatic.core.color.TextKey
import kotlin.random.Random
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Every TextKey referenced by any question resolves non-blank in BOTH locales (v16 §2). */
@RunWith(AndroidJUnit4::class)
class QuizContentTest {

    private fun context(tag: String): Context {
        val base = ApplicationProvider.getApplicationContext<Context>()
        val config = Configuration(base.resources.configuration).apply {
            setLocale(java.util.Locale.forLanguageTag(tag))
        }
        return base.createConfigurationContext(config)
    }

    @Test fun `all referenced keys resolve in English`() = assertAllResolve("en")
    @Test fun `all referenced keys resolve in Spanish`() = assertAllResolve("es")

    private fun assertAllResolve(tag: String) {
        val ctx = context(tag)
        val generated = QuizEngine.generate(6, Random(1), StaticBank.questions) +
            List(3) { NameGame.generate(Random(it.toLong())) }
        (generated + StaticBank.questions).forEach { q ->
            assertNotBlank(ctx, q.prompt)
            assertNotBlank(ctx, q.explanation)
            q.options.forEach { assertNotBlank(ctx, it.text) }
        }
    }

    private fun assertNotBlank(context: Context, text: QuizText) {
        if (text is QuizText.Key) {
            val rendered = context.getString(textRes(text.key), *text.args.map { "x" }.toTypedArray())
            assertTrue(
                "${text.key} missing for locale",
                rendered.isNotBlank() && rendered != text.key.name,
            )
        }
    }
}
