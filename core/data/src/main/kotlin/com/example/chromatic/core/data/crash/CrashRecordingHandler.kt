package com.example.chromatic.core.data.crash

import com.example.chromatic.core.data.BuildInfo
import android.os.Build

/**
 * Records locally, then always delegates — the system dialog still shows.
 * A failing store never breaks the delegate chain (tested).
 */
class CrashRecordingHandler(
    private val store: CrashStore,
    private val buildInfo: BuildInfo,
    private val delegate: Thread.UncaughtExceptionHandler?,
    private val nowMs: () -> Long,
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        runCatching {
            store.recordSync(
                CrashReport.from(
                    thread = thread, throwable = throwable,
                    versionName = buildInfo.versionName, versionCode = buildInfo.versionCode,
                    androidRelease = Build.VERSION.RELEASE ?: "unknown",
                    deviceModel = Build.MODEL ?: "unknown",
                    atMs = nowMs(),
                )
            )
        }
        delegate?.uncaughtException(thread, throwable)
    }
}
