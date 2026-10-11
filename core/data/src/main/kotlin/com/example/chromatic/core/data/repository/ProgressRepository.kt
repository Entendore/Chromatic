package com.example.chromatic.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface ProgressRepository {
    val completedLessonIds: Flow<Set<Long>>
    suspend fun toggle(lessonId: Long)
}

@Singleton
class DataStoreProgressRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : ProgressRepository {

    private val key = stringSetPreferencesKey("completed_lessons")

    override val completedLessonIds: Flow<Set<Long>> =
        dataStore.data.map { prefs -> prefs[key].orEmpty().mapNotNull(String::toLongOrNull).toSet() }

    /** Decide INSIDE the atomic edit — read-then-edit loses updates under rapid taps. */
    override suspend fun toggle(lessonId: Long) {
        dataStore.edit { prefs ->
            val id = lessonId.toString()
            val current = prefs[key].orEmpty()
            prefs[key] = if (id in current) current - id else current + id
        }
    }
}
