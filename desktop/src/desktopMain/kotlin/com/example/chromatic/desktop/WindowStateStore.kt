package com.example.chromatic.desktop

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import java.util.prefs.Preferences

/** java.util.prefs — desktop JVM only by design (v11 §5). */
object WindowStateStore {
    private val prefs = Preferences.userNodeForPackage(WindowStateStore::class.java)

    fun load(): WindowState? {
        val w = prefs.getDouble("w", 0.0)
        val h = prefs.getDouble("h", 0.0)
        if (w <= 0.0 || h <= 0.0) return null
        val x = prefs.getDouble("x", 0.0)
        val y = prefs.getDouble("y", 0.0)
        val position = if (x > 0.0 && y > 0.0) WindowPosition.Absolute(x.dp, y.dp)
        else WindowPosition.PlatformWindowPosition
        return WindowState(size = DpSize(w.dp, h.dp), position = position)
    }

    fun save(state: WindowState) {
        prefs.putDouble("w", state.size.width.value.toDouble())
        prefs.putDouble("h", state.size.height.value.toDouble())
        state.position.let { pos ->
            if (pos is WindowPosition.Absolute) {
                prefs.putDouble("x", pos.x.value.toDouble())
                prefs.putDouble("y", pos.y.value.toDouble())
            }
        }
    }
}
