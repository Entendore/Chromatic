package com.example.chromatic.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.chromatic.core.data.db.PaletteDao
import com.example.chromatic.core.data.db.PaletteEntity
import com.example.chromatic.core.data.model.SavedPalette
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaletteStoreMigration @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val json: Json,
    private val paletteDao: PaletteDao,
) {
    private val legacyKey = stringPreferencesKey("saved_palettes")

    /** Copies v1 DataStore palettes into Room once, then deletes the legacy key. */
    suspend fun migrateIfNeeded(): Int {
        val raw = dataStore.data.first()[legacyKey] ?: return 0
        val migrated = runCatching { json.decodeFromString<List<SavedPalette>>(raw) }
            .getOrDefault(emptyList())
            .onEach { p ->
                paletteDao.upsert(PaletteEntity(p.id, p.name, p.hexColors.joinToString(","), p.createdAtEpochMs))
            }
        dataStore.edit { it.remove(legacyKey) }
        return migrated.size
    }
}
