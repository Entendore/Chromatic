package com.example.chromatic.core.data.time

/** Injectable clock — achievements/streaks/review cooldowns become testable. */
fun interface AppClock { fun nowMs(): Long }

/** Install time (OS-owned, not stored) + Play availability (v17 §5). */
interface AppInfo {
    val firstInstallMs: Long
    val canRequestReview: Boolean
}
