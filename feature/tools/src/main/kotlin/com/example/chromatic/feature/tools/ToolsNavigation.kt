package com.example.chromatic.feature.tools

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.example.chromatic.core.color.Hsl
import com.example.chromatic.feature.tools.contrast.ContrastScreen
import com.example.chromatic.feature.tools.contrastgrid.ContrastGridScreen
import com.example.chromatic.feature.tools.gradient.GradientScreen
import com.example.chromatic.feature.tools.harmony.HarmonyScreen
import com.example.chromatic.feature.tools.illusionlab.IllusionLabScreen
import com.example.chromatic.feature.tools.mixlab.MixLabScreen
import com.example.chromatic.feature.tools.palette.PaletteBuilderScreen
import com.example.chromatic.feature.tools.palette.SnackbarWithAction
import com.example.chromatic.feature.tools.simulate.SimulateScreen
import com.example.chromatic.feature.tools.themestudio.ThemeStudioScreen
import kotlinx.serialization.Serializable

@Serializable data object HarmonyRoute
@Serializable data object ContrastRoute
@Serializable data object ContrastGridRoute
@Serializable data object PaletteRoute
@Serializable data object GradientRoute
@Serializable data object SimulateRoute
@Serializable data object MixLabRoute
@Serializable data object IllusionLabRoute
@Serializable data class ThemeStudioRoute(
    val seedHue: Float = 258f,
    val seedSaturation: Float = 0.72f,
    val seedLightness: Float = 0.52f,
)

fun NavHostController.openRoute(route: Any) {
    navigate(route) { launchSingleTop = true }
}

fun NavGraphBuilder.harmonyScreen(onShowSnackbar: (String) -> Unit) {
    composable<HarmonyRoute>(
        deepLinks = listOf(navDeepLink<HarmonyRoute>(uriPattern = "chromatic://harmony")),
    ) { HarmonyScreen(onShowSnackbar) }
}

fun NavGraphBuilder.contrastScreen(onShowSnackbar: (String) -> Unit, onOpenVision: () -> Unit) {
    composable<ContrastRoute>(
        deepLinks = listOf(navDeepLink<ContrastRoute>(uriPattern = "chromatic://contrast")),
    ) { ContrastScreen(onShowSnackbar, onOpenVision) }
}

fun NavGraphBuilder.contrastGridScreen(onShowSnackbar: (String) -> Unit) {
    composable<ContrastGridRoute>(
        deepLinks = listOf(navDeepLink<ContrastGridRoute>(uriPattern = "chromatic://grid")),
    ) { ContrastGridScreen(onShowSnackbar) }
}

fun NavGraphBuilder.paletteBuilderScreen(
    onShowSnackbar: SnackbarWithAction,
    onSendToThemeStudio: (Hsl) -> Unit,
) {
    composable<PaletteRoute> { PaletteBuilderScreen(onShowSnackbar, onSendToThemeStudio) }
}

fun NavGraphBuilder.gradientScreen(onShowSnackbar: (String) -> Unit) {
    composable<GradientRoute>(
        deepLinks = listOf(navDeepLink<GradientRoute>(uriPattern = "chromatic://gradient")),
    ) { GradientScreen(onShowSnackbar) }
}

fun NavGraphBuilder.themeStudioScreen(onShowSnackbar: (String) -> Unit) {
    composable<ThemeStudioRoute> { entry ->
        val route = entry.toRoute<ThemeStudioRoute>()
        ThemeStudioScreen(onShowSnackbar)
    }
}

fun NavGraphBuilder.simulateScreen() {
    composable<SimulateRoute>(
        deepLinks = listOf(navDeepLink<SimulateRoute>(uriPattern = "chromatic://vision")),
    ) { SimulateScreen() }
}

fun NavGraphBuilder.mixLabScreen() {
    composable<MixLabRoute> { MixLabScreen() }
}

fun NavGraphBuilder.illusionLabScreen() {
    composable<IllusionLabRoute> { IllusionLabScreen() }
}
