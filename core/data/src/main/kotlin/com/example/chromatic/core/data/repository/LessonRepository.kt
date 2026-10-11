package com.example.chromatic.core.data.repository

import com.example.chromatic.core.data.model.Lesson
import kotlinx.coroutines.flow.Flow

/** Contract lives in :core:data; the content-backed impl lives in :feature:learn. */
interface LessonRepository {
    val lessons: Flow<List<Lesson>>
    fun findById(id: Long): Flow<Lesson?>
}
