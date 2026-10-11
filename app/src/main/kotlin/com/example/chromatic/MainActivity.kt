package com.example.chromatic

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.chromatic.core.color.Rgb
import com.example.chromatic.core.color.ThemeGenerator
import com.example.chromatic.core.designsystem.components.cvdPreview
import com.example.chromatic.feature.learn.LearnRoute
import com.example.chromatic.feature.learn.OnboardingScreen
import com.example.chromatic.feature.settings.SettingsViewModel
import com.example.chromatic.feature.tools.ContrastRoute
import com.example.chromatic.feature.tools.HarmonyRoute
import com.example.chromatic.feature.tools.SimulateRoute
import com.example.chromatic.navigation.ChromaticApp
import com.example.chromatic.navigation.ShortcutRequest
import com.example.chromatic.theme.toColorScheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()
    private val pendingShortcut = MutableStateFlow<ShortcutRequest?>(null)
    private var shortcutNonce = 0L
    private val splashReady = MutableStateFlow(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()      // BEFORE super (v15 §0d)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Hold the splash until the STORED settings arrive — no theme flash.
        splashScreen.setKeepOnScreenCondition { !splashReady.value }
        lifecycleScope.launch {
            settingsViewModel.settings.first()
            splashReady.value = true
        }

        setContent {
            val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
            val shortcut by pendingShortcut.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()

            // Applied theme (v18): explicit user intent wins; unparseable seed degrades safely.
            val appliedScheme = remember(settings.appliedThemeSeedHex, systemDark) {
                settings.appliedThemeSeedHex
                    ?.let { Rgb.fromHexOrNull(it) }
                    ?.let { seed -> ThemeGenerator.generate(seed) }
                    ?.let { bundle -> (if (systemDark) bundle.dark else bundle.light).toColorScheme() }
            }

            com.example.chromatic.core.designsystem.theme.ChromaticTheme(
                darkTheme = settings.themeMode.resolve(systemDark),
                dynamicColor = settings.dynamicColor,
                overrideScheme = appliedScheme,
            ) {
                if (!settings.onboardingDone) {
                    OnboardingScreen(onDone = { settingsViewModel.onCompleteOnboarding() })
                } else {
                    Box(Modifier.fillMaxSize().cvdPreview(settings.cvdPreview)) {
                        ChromaticApp(
                            startDestination = routeFromIntent(intent) ?: LearnRoute,
                            onShortcut = shortcut,
                            onShortcutConsumed = { pendingShortcut.value = null },
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        routeFromIntent(intent)?.let { pendingShortcut.value = ShortcutRequest(it, shortcutNonce++) }
    }

    private fun routeFromIntent(intent: Intent?): Any? = when (intent?.getStringExtra(EXTRA_ROUTE)) {
        "contrast" -> ContrastRoute
        "harmony" -> HarmonyRoute
        "vision" -> SimulateRoute
        else -> null
    }

    companion object { const val EXTRA_ROUTE = "com.example.chromatic.extra.ROUTE" }
}
