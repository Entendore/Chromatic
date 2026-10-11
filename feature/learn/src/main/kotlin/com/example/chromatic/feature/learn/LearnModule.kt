package com.example.chromatic.feature.learn

import com.example.chromatic.core.data.repository.LessonRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LearnModule {
    @Binds @Singleton
    abstract fun bindLessonRepository(impl: ResourceLessonRepository): LessonRepository
}
