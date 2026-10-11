package com.example.chromatic.core.data.db

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "saved_palettes")
data class PaletteEntity(
    @PrimaryKey val id: String,
    val name: String,
    /** Hex values comma-joined, e.g. "#0D47A1,#1976D2". */
    val hexColors: String,
    val createdAtEpochMs: Long,
)

@Entity(tableName = "quiz_results")
data class QuizResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gameType: String,
    val score: Int,
    val maxScore: Int,
    val createdAtEpochMs: Long,
)

@Dao
interface PaletteDao {
    @Upsert suspend fun upsert(entity: PaletteEntity)
    @Query("SELECT * FROM saved_palettes ORDER BY createdAtEpochMs DESC")
    fun observeAll(): Flow<List<PaletteEntity>>
    @Query("DELETE FROM saved_palettes WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface QuizResultDao {
    @Insert suspend fun insert(entity: QuizResultEntity)
    @Query("SELECT * FROM quiz_results ORDER BY createdAtEpochMs DESC LIMIT 50")
    fun observeRecent(): Flow<List<QuizResultEntity>>
}

@Database(entities = [PaletteEntity::class, QuizResultEntity::class], version = 1, exportSchema = true)
abstract class ChromaticDatabase : RoomDatabase() {
    abstract fun paletteDao(): PaletteDao
    abstract fun quizResultDao(): QuizResultDao
}
