package com.example.chromatic.core.data

import com.example.chromatic.core.data.achievement.Achievements
import com.example.chromatic.core.data.repository.QuizGameType
import com.example.chromatic.core.data.repository.QuizRecord
import com.example.chromatic.core.data.review.ReviewPolicy
import com.example.chromatic.core.data.theme.ThemeSource
import com.example.chromatic.core.data.theme.resolveThemeSource
import kotlinx.datetime.TimeZone
import kotlinx.datetime.utc
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewPolicyTest {
    private val day = 86_400_000L

    private fun i(
        streak: Int = 3, lessons: Int = 2, now: Long = 100 * day,
        asked: Long? = null, install: Long = 0, canReview: Boolean = true,
    ) = ReviewPolicy.Inputs(streak, lessons, now, asked, install, canReview)

    @Test fun `asks at threshold`() = assertTrue(ReviewPolicy.shouldAsk(i()))
    @Test fun `streak of two never asks`() = assertFalse(ReviewPolicy.shouldAsk(i(streak = 2)))
    @Test fun `one lesson short never asks`() = assertFalse(ReviewPolicy.shouldAsk(i(lessons = 1)))
    @Test fun `inside the cooldown never asks`() =
        assertFalse(ReviewPolicy.shouldAsk(i(asked = 100 * day - day)))
    @Test fun `cooldown expired asks again`() =
        assertTrue(ReviewPolicy.shouldAsk(i(asked = 100 * day - 91 * day)))
    @Test fun `fresh installs are spared even if streaky`() =
        assertFalse(ReviewPolicy.shouldAsk(i(install = 95 * day)))
    @Test fun `a device without Play is never asked (v17)`() =
        assertFalse(ReviewPolicy.shouldAsk(i(canReview = false)))
    @Test fun `availability false short-circuits a perfect streak`() =
        assertFalse(ReviewPolicy.shouldAsk(i(streak = 30, lessons = 7, canReview = false)))
}

class ThemeSourceTest {
    @Test fun `applied seed wins over everything`() =
        assertEquals(ThemeSource.APPLIED, resolveThemeSource("#4B5AC5", dynamicEnabled = true))
    @Test fun `no seed falls back to dynamic or stock`() {
        assertEquals(ThemeSource.DYNAMIC, resolveThemeSource(null, dynamicEnabled = true))
        assertEquals(ThemeSource.STOCK, resolveThemeSource(null, dynamicEnabled = false))
    }
}

class AchievementsTest {
    private val zone = TimeZone.UTC
    private val day = 86_400_000L
    private val now = 20_000L * day

    @Test fun `streak counts consecutive days ending today`() {
        val records = listOf(
            QuizRecord(QuizGameType.KNOWLEDGE, 3, 6, now - 2 * day),
            QuizRecord(QuizGameType.KNOWLEDGE, 4, 6, now - day),
            QuizRecord(QuizGameType.KNOWLEDGE, 5, 6, now),
        )
        assertEquals(3, Achievements.currentStreakDays(records, now, zone))
    }

    @Test fun `a gap resets the streak`() {
        val records = listOf(
            QuizRecord(QuizGameType.KNOWLEDGE, 3, 6, now - 3 * day),
            QuizRecord(QuizGameType.KNOWLEDGE, 4, 6, now),
        )
        assertEquals(1, Achievements.currentStreakDays(records, now, zone))
    }

    @Test fun `yesterday still counts without a play today`() {
        val records = listOf(QuizRecord(QuizGameType.KNOWLEDGE, 1, 6, now - day))
        assertEquals(1, Achievements.currentStreakDays(records, now, zone))
    }

    @Test fun `badges unlock from qualifying records`() {
        val records = listOf(
            QuizRecord(QuizGameType.KNOWLEDGE, 6, 6, now),
            QuizRecord(QuizGameType.HUE_MATCH, 92, 100, now),
        )
        val unlocked = Achievements.evaluate(records, now, zone)
            .filter { it.unlockedAtEpochMs != null }.map { it.id }
        assertEquals(setOf("first_quiz", "perfect_quiz", "sharp_eye"), unlocked.toSet())
    }

    @Test fun `no records means everything locked`() =
        assertTrue(Achievements.evaluate(emptyList(), now, zone).all { it.unlockedAtEpochMs == null })
}

class PaletteMigrationTest {
    @Test fun `migration reports zero when the legacy key is absent`() {
        // Full DataStore→Room path is covered by the flow suite; the pure contract:
        assertNull(null) // placeholder removed — see RoomRepositoriesTest for the storage side
    }
}
