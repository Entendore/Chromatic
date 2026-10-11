package com.example.chromatic.feature.settings

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable data object SettingsRoute
@Serializable data object CrashReportsRoute
@Serializable data object HelpRoute

fun NavGraphBuilder.settingsScreen(onBack: () -> Unit, onOpenCrashReports: () -> Unit, onOpenHelp: () -> Unit) {
    composable<SettingsRoute> { SettingsScreen(onBack, onOpenCrashReports, onOpenHelp) }
}

fun NavGraphBuilder.crashReportsScreen(onBack: () -> Unit) {
    composable<CrashReportsRoute> { CrashReportsScreen(onBack) }
}

fun NavGraphBuilder.helpScreen(onBack: () -> Unit) {
    composable<HelpRoute> { HelpScreen(onBack) }
}
