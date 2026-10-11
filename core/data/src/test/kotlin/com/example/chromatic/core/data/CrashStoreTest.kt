package com.example.chromatic.core.data

import com.example.chromatic.core.data.crash.CrashReport
import com.example.chromatic.core.data.crash.CrashRecordingHandler
import com.example.chromatic.core.data.crash.CrashStore
import com.example.chromatic.core.data.crash.FileCrashStore
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class CrashStoreTest {

    @get:Rule val tmp = TemporaryFolder()

    private fun store() = FileCrashStore(tmp.newFolder()) { 1_000L }

    private fun report(at: Long) = CrashReport(
        at, "1.0.0", 42, "main",
        "java.lang.IllegalStateException", "boom\n  at App.onCrash(App.kt:1)", "14", "Pixel 7",
    )

    @Test fun `reports read back newest first`() {
        val s = store()
        s.recordSync(report(1_000)); s.recordSync(report(2_000)); s.recordSync(report(3_000))
        assertEquals(listOf(3_000L, 2_000L, 1_000L), s.reports().map { it.occurredAtEpochMs })
    }

    @Test fun `prune keeps only the newest ten`() {
        val s = store()
        repeat(15) { s.recordSync(report(it.toLong() * 1_000)) }
        assertEquals(10, s.reports().size)
        assertEquals(14_000L, s.reports().first().occurredAtEpochMs)
    }

    @Test fun `a truncated report from a mid-write crash never breaks the reader`() {
        val s = store()
        s.recordSync(report(1_000))
        File(s.reportsDir().absolutePath + File.separator + "2_000.json").writeText(
            "{\"occurredAtEpochMs\":2000,\"thr"
        )
        assertEquals(1, s.reports().size)
        s.clear()
        assertTrue(s.reports().isEmpty())
    }

    @Test fun `clear removes every file`() {
        val s = store()
        repeat(3) { s.recordSync(report(it.toLong())) }
        s.clear()
        assertTrue(s.reports().isEmpty())
    }
}

class CrashRecordingHandlerTest {

    private class RecordingStore : CrashStore {
        var recorded: CrashReport? = null
        var throwOnRecord = false
        override fun recordSync(report: CrashReport) {
            if (throwOnRecord) error("disk full")
            recorded = report
        }
        override fun reports() = emptyList<CrashReport>()
        override fun clear() {}
    }

    @Test fun `records then delegates to the system handler`() {
        val store = RecordingStore()
        var delegated: Throwable? = null
        val handler = CrashRecordingHandler(
            store, BuildInfo("1.0.0", 42, true), { _, e -> delegated = e },
        ) { 5L }
        val boom = IllegalStateException("x")
        handler.uncaughtException(Thread.currentThread(), boom)
        assertEquals(boom, delegated)
        assertEquals(5L, store.recorded?.occurredAtEpochMs)
        assertEquals("main", store.recorded?.threadName)
    }

    @Test fun `a failing store never breaks the delegate chain (v17 contract)`() {
        val store = RecordingStore().apply { throwOnRecord = true }
        var delegated: Throwable? = null
        val handler = CrashRecordingHandler(
            store, BuildInfo("1.0.0", 42, true), { _, e -> delegated = e },
        ) { 5L }
        handler.uncaughtException(Thread.currentThread(), RuntimeException("y"))
        assertNotNull(delegated)
    }
}
