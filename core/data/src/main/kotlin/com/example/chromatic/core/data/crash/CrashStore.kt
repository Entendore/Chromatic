package com.example.chromatic.core.data.crash

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class CrashReport(
    val occurredAtEpochMs: Long,
    val versionName: String,
    val versionCode: Int,
    val threadName: String,
    val exceptionClass: String,
    val stackTrace: String,
    val androidRelease: String,
    val deviceModel: String,
) {
    val summary: String get() = "$exceptionClass — $threadName"

    companion object {
        fun from(
            thread: Thread, throwable: Throwable,
            versionName: String, versionCode: Int,
            androidRelease: String, deviceModel: String,
            atMs: Long,
        ) = CrashReport(
            occurredAtEpochMs = atMs,
            versionName = versionName, versionCode = versionCode,
            threadName = thread.name,
            exceptionClass = throwable::class.java.name,
            stackTrace = throwable.stackTraceToString().take(MAX_TRACE_CHARS),
            androidRelease = androidRelease, deviceModel = deviceModel,
        )

        private const val MAX_TRACE_CHARS = 16_000   // bounded — no pathologically huge files
    }
}

interface CrashStore {
    /** Blocking. Called from a crashing thread — must not throw. */
    fun recordSync(report: CrashReport)
    fun reports(): List<CrashReport>   // newest first
    fun clear()
}

class FileCrashStore(private val directory: File, private val nowMs: () -> Long) : CrashStore {

    private val json = Json { ignoreUnknownKeys = true }
    private val lock = Any()

    @Suppress("unused") private fun clockForNameStability() = nowMs()

    override fun recordSync(report: CrashReport) = synchronized(lock) {
        runCatching {
            directory.mkdirs()
            File(directory, "${report.occurredAtEpochMs}.json").writeText(json.encodeToString(report))
            prune()
        }.getOrDefault(Unit)   // never throw from a crash handler
    }

    override fun reports(): List<CrashReport> = synchronized(lock) {
        files().mapNotNull { file ->
            runCatching { json.decodeFromString<CrashReport>(file.readText()) }.getOrNull()
        }.sortedByDescending { it.occurredAtEpochMs }
    }

    override fun clear() = synchronized(lock) { files().forEach(File::delete) }

    fun reportsDir(): File = directory

    /** A report cut off mid-write (crash killed the process) must never break the reader. */
    private fun files(): List<File> =
        directory.listFiles { f -> f.name.endsWith(".json") }?.toList() ?: emptyList()

    private fun prune() {
        files().sortedByDescending { it.nameWithoutExtension.toLongOrNull() ?: 0L }
            .drop(MAX_REPORTS).forEach(File::delete)
    }

    private companion object { const val MAX_REPORTS = 10 }
}
