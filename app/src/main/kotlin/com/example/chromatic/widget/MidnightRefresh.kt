package com.example.chromatic.widget

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Duration
import java.time.LocalDateTime
import timber.log.Timber

/**
 * Fires once per local midnight: refreshes the widget, then reschedules itself.
 * WorkManager persists pending work across reboots — a single schedule point
 * (Application.onCreate) suffices; no BOOT receiver (v7 §2.3).
 */
@HiltWorker
class MidnightRefreshWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        ColorOfDayWidget().updateAll(applicationContext)
        MidnightRefreshScheduler.schedule(applicationContext)
        Timber.tag("MidnightRefresh").d("widget refreshed; rescheduled for next local midnight")
        return Result.success()
    }

    companion object { const val UNIQUE_NAME = "midnight_widget_refresh" }
}

object MidnightRefreshScheduler {

    fun schedule(context: Context) {
        val now = LocalDateTime.now()
        val untilMidnight = Duration.between(now, now.toLocalDate().plusDays(1).atStartOfDay())
        val request = OneTimeWorkRequestBuilder<MidnightRefreshWorker>()
            .setInitialDelay(untilMidnight)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            MidnightRefreshWorker.UNIQUE_NAME,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }
}
