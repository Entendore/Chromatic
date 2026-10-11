package com.example.chromatic.di

import com.example.chromatic.BuildConfig
import com.example.chromatic.core.data.BuildInfo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    /** :app owns BuildConfig — the version surface for Settings About + crash reports. */
    @Provides @Singleton
    fun provideBuildInfo(): BuildInfo =
        BuildInfo(BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE, BuildConfig.DEBUG)
}
