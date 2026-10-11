package com.example.chromatic.feature.tools.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ToolsModule {
    @Binds @Singleton
    abstract fun bindPhotoPaletteExtractor(impl: DefaultPhotoPaletteExtractor): PhotoPaletteExtractor

    @Binds @Singleton
    abstract fun bindPaletteImageSharer(impl: DefaultPaletteImageSharer): PaletteImageSharer
}
