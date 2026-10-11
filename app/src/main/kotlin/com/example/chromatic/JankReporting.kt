package com.example.chromatic

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import androidx.metrics.performance.JankStats
import androidx.metrics.performance.PerformanceMetricsState
import com.example.chromatic.core.designsystem.util.findActivity
import timber.log.Timber

/** Debug-only collection; the screen name rides every frame log (v10 §0b). */
@Composable
fun JankStatsEffect(currentRoute: String?) {
    if (!BuildConfig.DEBUG) return
    val view = LocalView.current
    val holder = remember { PerformanceMetricsState.getHolderForHierarchy(view) }
    val window = remember(view) { (view.context.findActivity() as? Activity)?.window }

    DisposableEffect(window) {
        window ?: return@DisposableEffect onDispose { }
        val stats = JankStats.createAndCollectFromWindow(window, holder)
        stats.isTrackingEnabled = true
        stats.jankListener = { frame ->
            Timber.tag("JankStats").d(
                "%s ms — %s",
                frame.durationMs,
                frame.states.joinToString { it.stateName ?: "" },
            )
        }
        onDispose { stats.isTrackingEnabled = false }
    }

    DisposableEffect(currentRoute) {
        // Type-safe routes are FQCNs — parse the simple name, strip path args (v10 §0b).
        val screenName = currentRoute
            ?.substringBefore('?')?.substringBefore('/')
            ?.substringAfterLast('.') ?: "unknown"
        holder.state.putState("screen", screenName)
        onDispose { holder.state.removeState("screen") }
    }
}
