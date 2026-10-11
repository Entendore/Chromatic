package com.example.chromatic.core.data.repository

import com.example.chromatic.core.data.db.QuizResultDao
import com.example.chromatic.core.data.db.QuizResultEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class QuizGameType { KNOWLEDGE, HUE_MATCH, NAME_COLOR, LIGHTNESS }

data class QuizRecord(val gameType: QuizGameType, val score: Int, val maxScore: Int, val atEpochMs: Long)

data class QuizStats(val totalPlayed: Int = 0, val bestKnowledge: Int = 0, val bestHueMatch: Int = 0)

interface QuizRepository {
    val stats: Flow<QuizStats>
    val records: Flow<List<QuizRecord>>
    suspend fun record(gameType: QuizGameType, score: Int, maxScore: Int)
}

@Singleton
class RoomQuizRepository @Inject constructor(private val dao: QuizResultDao) : QuizRepository {

    override val records: Flow<List<QuizRecord>> = dao.observeRecent().map { list ->
        list.mapNotNull { e ->
            val type = QuizGameType.entries.firstOrNull { it.name == e.gameType }
                ?: return@mapNotNull null   // legacy/unknown rows filtered (v12 §5)
            QuizRecord(type, e.score, e.maxScore, e.createdAtEpochMs)
        }
    }

    override val stats: Flow<QuizStats> = records.map { recent ->
        QuizStats(
            totalPlayed = recent.size,
            bestKnowledge = recent.filter { it.gameType == QuizGameType.KNOWLEDGE }
                .maxOfOrNull { it.score } ?: 0,
            bestHueMatch = recent.filter { it.gameType == QuizGameType.HUE_MATCH }
                .maxOfOrNull { it.score } ?: 0,
        )
    }

    override suspend fun record(gameType: QuizGameType, score: Int, maxScore: Int) {
        dao.insert(
            QuizResultEntity(
                gameType = gameType.name,
                score = score,
                maxScore = maxScore,
                createdAtEpochMs = System.currentTimeMillis(),
            )
        )
    }
}
