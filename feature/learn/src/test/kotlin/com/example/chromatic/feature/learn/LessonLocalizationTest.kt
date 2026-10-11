package com.example.chromatic.feature.learn

import android.content.Context
import android.content.res.Configuration
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

/** The v15 §0a lesson mechanized: content completeness is a TEST, not a checklist line. */
@RunWith(AndroidJUnit4::class)
class LessonLocalizationTest {

    private fun repository(tag: String): ResourceLessonRepository {
        val base = ApplicationProvider.getApplicationContext<Context>()
        val config = Configuration(base.resources.configuration).apply {
            setLocale(Locale.forLanguageTag(tag))
        }
        return ResourceLessonRepository(base.createConfigurationContext(config))
    }

    @Test fun `all seven lessons resolve complete content in English`() = assertComplete("en")
    @Test fun `all seven lessons resolve complete content in Spanish`() = assertComplete("es")

    private fun assertComplete(tag: String) = runTest {
        val lessons = repository(tag).lessons.first()
        assertEquals(7, lessons.size)
        lessons.forEach { lesson ->
            assertTrue(lesson.title.isNotBlank())
            assertTrue(lesson.summary.isNotBlank())
            assertTrue("lesson ${lesson.id} sections", lesson.sections.isNotEmpty())
            lesson.sections.forEach { s ->
                assertTrue(s.heading.isNotBlank())
                assertTrue(s.body.isNotBlank())
            }
        }
    }
}
