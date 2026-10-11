package com.example.chromatic.core.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.chromatic.core.data.db.ChromaticDatabase
import com.example.chromatic.core.data.db.QuizResultEntity
import com.example.chromatic.core.data.model.SavedPalette
import com.example.chromatic.core.data.repository.RoomPaletteRepository
import com.example.chromatic.core.data.repository.RoomQuizRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomRepositoriesTest {

    private val db = Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext(), ChromaticDatabase::class.java,
    ).allowMainThreadQueries().build()

    @After fun tearDown() = db.close()

    @Test fun `palettes map to domain and sort newest first`() = runTest {
        val repo = RoomPaletteRepository(db.paletteDao())
        repo.save(SavedPalette("1", "Old", listOf("#000000"), 1_000))
        repo.save(SavedPalette("2", "New", listOf("#FFFFFF", "#000000"), 2_000))
        val list = repo.palettes.first()
        assertEquals(listOf("2", "1"), list.map { it.id })
        assertEquals(listOf("#FFFFFF", "#000000"), list[0].hexColors)
    }

    @Test fun `upsert with the same id replaces`() = runTest {
        val repo = RoomPaletteRepository(db.paletteDao())
        repo.save(SavedPalette("1", "A", listOf("#000000"), 1))
        repo.save(SavedPalette("1", "A2", listOf("#111111"), 2))
        assertEquals(listOf("A2"), repo.palettes.first().map { it.name })
    }

    @Test fun `delete removes exactly the target`() = runTest {
        val repo = RoomPaletteRepository(db.paletteDao())
        repo.save(SavedPalette("1", "A", listOf("#000000"), 1))
        repo.save(SavedPalette("2", "B", listOf("#000000"), 2))
        repo.delete("1")
        assertEquals(listOf("2"), repo.palettes.first().map { it.id })
    }

    @Test fun `quiz stats aggregate best scores and filter unknown game types`() = runTest {
        val dao = db.quizResultDao()
        dao.insert(QuizResultEntity(0, "KNOWLEDGE", 4, 6, 1))
        dao.insert(QuizResultEntity(0, "KNOWLEDGE", 6, 6, 2))
        dao.insert(QuizResultEntity(0, "HUE_MATCH", 80, 100, 3))
        dao.insert(QuizResultEntity(0, "LEGACY_V1", 99, 100, 4))

        val repo = RoomQuizRepository(dao)
        val stats = repo.stats.first()
        assertEquals(3, repo.records.first().size)
        assertEquals(6, stats.bestKnowledge)
        assertEquals(80, stats.bestHueMatch)
        assertEquals(3, stats.totalPlayed)
    }
}
