package com.example.chromatic.core.data.repository

import com.example.chromatic.core.data.db.PaletteDao
import com.example.chromatic.core.data.db.PaletteEntity
import com.example.chromatic.core.data.model.SavedPalette
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface PaletteRepository {
    val palettes: Flow<List<SavedPalette>>
    suspend fun save(palette: SavedPalette)
    suspend fun delete(id: String)
}

@Singleton
class RoomPaletteRepository @Inject constructor(private val dao: PaletteDao) : PaletteRepository {

    override val palettes: Flow<List<SavedPalette>> =
        dao.observeAll().map { list -> list.map(PaletteEntity::toDomain) }

    override suspend fun save(palette: SavedPalette) = dao.upsert(palette.toEntity())

    override suspend fun delete(id: String) = dao.deleteById(id)

    private fun PaletteEntity.toDomain() = SavedPalette(
        id = id,
        name = name,
        hexColors = hexColors.split(',').filter { it.isNotBlank() },
        createdAtEpochMs = createdAtEpochMs,
    )

    private fun SavedPalette.toEntity() = PaletteEntity(
        id = id, name = name,
        hexColors = hexColors.joinToString(","),
        createdAtEpochMs = createdAtEpochMs,
    )
}
