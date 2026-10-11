package com.example.chromatic.core.data.di

import android.content.Context
import android.content.pm.PackageManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.example.chromatic.core.data.BuildInfo
import com.example.chromatic.core.data.crash.CrashStore
import com.example.chromatic.core.data.crash.FileCrashStore
import com.example.chromatic.core.data.db.ChromaticDatabase
import com.example.chromatic.core.data.db.PaletteDao
import com.example.chromatic.core.data.db.QuizResultDao
import com.example.chromatic.core.data.repository.RoomPaletteRepository
import com.example.chromatic.core.data.repository.RoomQuizRepository
import com.example.chromatic.core.data.repository.DataStoreProgressRepository
import com.example.chromatic.core.data.repository.DataStoreSettingsRepository
import com.example.chromatic.core.data.repository.LessonRepository
import com.example.chromatic.core.data.repository.PaletteRepository
import com.example.chromatic.core.data.repository.ProgressRepository
import com.example.chromatic.core.data.repository.QuizRepository
import com.example.chromatic.core.data.repository.SettingsRepository
import com.example.chromatic.core.data.review.PlayAvailability
import com.example.chromatic.core.data.time.AppClock
import com.example.chromatic.core.data.time.AppInfo
import androidx.core.content.pm.PackageInfoCompat
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    /** Application-scoped coroutine work (migrations) outlives any screen. */
    @Provides @Singleton
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Provides @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    /** Corruption policy lives HERE and only here (v9 §2) — no blanket catches downstream. */
    @Provides @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler(produceNewFile = { emptyPreferences() }),
        ) { context.preferencesDataStoreFile("chromatic_settings") }

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ChromaticDatabase =
        Room.databaseBuilder(context, ChromaticDatabase::class.java, "chromatic.db").build()

    // The v3 gap fixed: repositories inject DAOs directly, so they must be provided.
    @Provides fun providePaletteDao(db: ChromaticDatabase): PaletteDao = db.paletteDao()
    @Provides fun provideQuizResultDao(db: ChromaticDatabase): QuizResultDao = db.quizResultDao()

    @Provides @Singleton
    fun provideAppClock(): AppClock = AppClock { System.currentTimeMillis() }

    @Provides @Singleton
    fun provideAppInfo(@ApplicationContext context: Context): AppInfo = object : AppInfo {
        override val firstInstallMs: Long = runCatching {
            PackageInfoCompat.getFirstInstallTime(
                context.packageManager.getPackageInfo(context.packageName, 0), TimeUnit.MILLISECONDS,
            )
        }.getOrDefault(0L)
        override val canRequestReview: Boolean =
            PlayAvailability.canRequestReview(context.packageManager)
    }

    @Provides @Singleton
    fun provideCrashStore(@ApplicationContext context: Context, clock: AppClock): CrashStore =
        FileCrashStore(java.io.File(context.filesDir, "crash_reports")) { clock.nowMs() }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    // LessonRepository is bound in :feature:learn (content owns its impl — v7/v18 note).
    @Binds @Singleton abstract fun bindProgressRepository(impl: DataStoreProgressRepository): ProgressRepository
    @Binds @Singleton abstract fun bindPaletteRepository(impl: RoomPaletteRepository): PaletteRepository
    @Binds @Singleton abstract fun bindQuizRepository(impl: RoomQuizRepository): QuizRepository
    @Binds @Singleton abstract fun bindSettingsRepository(impl: DataStoreSettingsRepository): SettingsRepository
}
