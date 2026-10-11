package com.example.chromatic

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.chromatic.core.data.BuildInfo
import com.example.chromatic.core.data.crash.CrashRecordingHandler
import com.example.chromatic.core.data.crash.CrashStore
import com.example.chromatic.core.data.repository.PaletteStoreMigration
import com.example.chromatic.core.data.time.AppClock
import com.example.chromatic.widget.MidnightRefreshScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltAndroidApp
class ChromaticApplication : Application(), Configuration.Provider {

    @Inject lateinit var migration: PaletteStoreMigration
    @Inject lateinit var applicationScope: CoroutineScope
    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var crashStore: CrashStore
    @Inject lateinit var buildInfo: BuildInfo
    @Inject lateinit var clock: AppClock

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())

        // Local-only crash capture (v17): record → delegate. Installed before anything else.
        val systemHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(
            CrashRecordingHandler(crashStore, buildInfo, systemHandler) { clock.nowMs() }
        )

        applicationScope.launch { migration.migrateIfNeeded() }
        MidnightRefreshScheduler.schedule(this)
    }
}
