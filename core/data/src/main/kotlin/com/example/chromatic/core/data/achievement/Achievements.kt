package com.example.chromatic.core.data.achievement

import com.example.chromatic.core.data.repository.QuizGameType
import com.example.chromatic.core.data.repository.QuizRecord
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class Achievement(val id: String, val unlockedAtEpochMs: Long?)

object Achievements {

    val ALL_IDS = listOf("first_quiz", "perfect_quiz", "sharp_eye", "ten_rounds", "three_day_streak")

    fun evaluate(records: List<QuizRecord>, nowEpochMs: Long, zone: TimeZone = TimeZone.currentSystemDefault()):
        List<Achievement> = ALL_IDS.map { id ->
        val unlockedAt = when (id) {
            "first_quiz" -> records.minOfOrNull { it.atEpochMs }
            "perfect_quiz" -> records
                .filter { it.gameType == QuizGameType.KNOWLEDGE && it.maxScore > 0 && it.score == it.maxScore }
                .minOfOrNull { it.atEpochMs }
            "sharp_eye" -> records
                .filter { it.gameType == QuizGameType.HUE_MATCH && it.maxScore > 0 && it.score * 10 >= it.maxScore * 9 }
                .minOfOrNull { it.atEpochMs }
            "ten_rounds" -> records.map { it.atEpochMs }.sorted().getOrNull(9)
            "three_day_streak" ->
                if (currentStreakDays(records, nowEpochMs, zone) >= 3) records.maxOfOrNull { it.atEpochMs } else null
            else -> null
        }
        Achievement(id, unlockedAt)
    }

    /** Consecutive-day play count ending today (yesterday still counts as grace). */
    fun currentStreakDays(records: List<QuizRecord>, nowEpochMs: Long, zone: TimeZone = TimeZone.currentSystemDefault()):
        Int {
        if (records.isEmpty()) return 0
        fun toLocalDate(ms: Long): LocalDate =
            Instant.fromEpochMilliseconds(ms).toLocalDateTime(zone).date
        val played = records.map { toLocalDate(it.atEpochMs) }.toHashSet()
        val today = toLocalDate(nowEpochMs)
        var cursor = if (today in played) today else today.minus(1, kotlinx.datetime.DateTimeUnit.DAY)
        var streak = 0
        while (cursor in played) {
            streak++
            cursor = cursor.minus(1, kotlinx.datetime.DateTimeUnit.DAY)
        }
        return streak
    }
}
