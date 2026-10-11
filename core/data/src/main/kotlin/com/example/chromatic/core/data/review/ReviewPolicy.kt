package com.example.chromatic.core.data.review

import android.content.pm.PackageManager
import androidx.core.content.pm.PackageInfoCompat

object PlayAvailability {
    fun canRequestReview(pm: PackageManager): Boolean = runCatching {
        pm.getPackageInfo("com.android.vending", 0); true
    }.getOrDefault(false)
}

/**
 * Pure ask-for-review policy (v16/v18): genuine engagement + install grace +
 * 90-day cooldown + Play present. Total refusal otherwise.
 */
object ReviewPolicy {
    const val MIN_STREAK_DAYS = 3
    const val MIN_LESSONS = 2
    const val COOLDOWN_MS = 90L * 86_400_000
    const val INSTALL_GRACE_MS = 7L * 86_400_000

    data class Inputs(
        val streakDays: Int,
        val completedLessons: Int,
        val nowMs: Long,
        val lastAskedMs: Long?,
        val firstInstallMs: Long,
        val canRequestReview: Boolean = true,
    )

    fun shouldAsk(i: Inputs): Boolean =
        i.canRequestReview &&
            i.streakDays >= MIN_STREAK_DAYS &&
            i.completedLessons >= MIN_LESSONS &&
            i.nowMs - i.firstInstallMs >= INSTALL_GRACE_MS &&
            (i.lastAskedMs == null || i.nowMs - i.lastAskedMs >= COOLDOWN_MS)
}
