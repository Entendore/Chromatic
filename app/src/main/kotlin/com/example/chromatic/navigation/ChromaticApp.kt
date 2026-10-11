package com.example.chromatic.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationSuiteScaffold
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.metrics.performance.PerformanceMetricsState
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.chromatic.feature.learn.LearnRoute
import com.example.chromatic.feature.learn.learnScreen
import com.example.chromatic.feature.quiz.quizScreen
import com.example.chromatic.feature.tools.IllusionLabRoute
import com.example.chromatic.feature.tools.MixLabRoute
import com.example.chromatic.feature.tools.SimulateRoute
import com.example.chromatic.feature.tools.ThemeStudioRoute
import com.example.chromatic.feature.tools.contrastGridScreen
import com.example.chromatic.feature.tools.contrastScreen
import com.example.chromatic.feature.tools.gradientScreen
import com.example.chromatic.feature.tools.harmonyScreen
import com.example.chromatic.feature.tools.illusionLabScreen
import com.example.chromatic.feature.tools.mixLabScreen
import com.example.chromatic.feature.tools.openRoute
import com.example.chromatic.feature.tools.palette.PaletteRoute
import com.example.chromatic.feature.tools.paletteBuilderScreen
import com.example.chromatic.feature.tools.palette.SnackbarWithAction
import com.example.chromatic.feature.tools.simulateScreen
import com.example.chromatic.feature.tools.themeStudioScreen
import com.example.chromatic.feature.settings.CrashReportsRoute
import com.example.chromatic.feature.settings.HelpRoute
import com.example.chromatic.feature.settings.crashReportsScreen
import com.example.chromatic.feature.settings.helpScreen
import com.example.chromatic.feature.settings.settingsScreen
import com.example.chromatic.JankStatsEffect
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ChromaticApp(
    startDestination: Any = LearnRoute,
    onShortcut: ShortcutRequest? = null,
    onShortcutConsumed: () -> Unit = {},
    navController: NavHostController = rememberNavController(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val showSnackbarFull: SnackbarWithAction = { msg, action, onAction ->
        scope.launch {
            if (snackbarHostState.showSnackbar(msg, actionLabel = action) == SnackbarResult.ActionPerformed) {
                onAction?.invoke()
            }
        }
    }
    val showSnackbarSimple: (String) -> Unit = { showSnackbarFull(it, null, null) }

    LaunchedEffect(onShortcut) {
        onShortcut?.let {
            navController.openRoute(it.route)
            onShortcutConsumed()
        }
    }

    // Jank attribution (v9/v10): one source of truth for the current screen name.
    JankStatsEffect(currentDestination?.route)

    val showNavSuite = TopLevelDestination.entries.any {
        currentDestination?.hasRoute(it.route::class) == true
    }

    if (showNavSuite) {
        // The suite owns the scaffold — no outer Scaffold (v11 §0d single-padding fix).
        NavigationSuiteScaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            navigationSuiteItems = {
                TopLevelDestination.entries.forEach { dest ->
                    val selected = currentDestination?.hasRoute(dest.route::class) == true
                    item(
                        selected = selected,
                        onClick = {
                            navController.navigate(dest.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                if (selected) dest.selectedIcon else dest.unselectedIcon,
                                contentDescription = dest.label(),
                            )
                        },
                        label = { Text(dest.label()) },
                    )
                }
            },
        ) { padding ->
            ChromaticNavHost(navController, startDestination, showSnackbarFull, showSnackbarSimple,
                Modifier.padding(padding))
        }
    } else {
        Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
            ChromaticNavHost(navController, startDestination, showSnackbarFull, showSnackbarSimple,
                Modifier.padding(padding))
        }
    }
}

@Composable
private fun ChromaticNavHost(
    navController: NavHostController,
    startDestination: Any,
    showSnackbarFull: SnackbarWithAction,
    showSnackbarSimple: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = { slideInHorizontally(initialOffsetX = { it / 4 }) + fadeIn(tween(220)) },
        exitTransition = { fadeOut(tween(180)) },
        popEnterTransition = { fadeIn(tween(220)) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it / 4 }) + fadeOut(tween(180)) },
    ) {
        learnScreen(
            onOpenQuiz = { navController.navigate(TopLevelDestination.QUIZ.route) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true; restoreState = true
            } },
            onOpenVision = { navController.openRoute(SimulateRoute) },
            onOpenGradient = { navController.openRoute(com.example.chromatic.feature.tools.GradientRoute) },
            onOpenThemeStudio = { navController.openRoute(ThemeStudioRoute()) },
            onOpenMixLab = { navController.openRoute(MixLabRoute) },
            onOpenIllusionLab = { navController.openRoute(IllusionLabRoute) },
            onOpenGrid = { navController.openRoute(ContrastGridRoute) },
            onOpenSettings = { navController.openRoute(com.example.chromatic.feature.settings.SettingsRoute) },
            onPractice = { practice ->
                val route = practice.toNavigationRoute()
                navController.navigate(route) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true; restoreState = true
                }
            },
        )
        quizScreen()
        harmonyScreen(showSnackbarSimple)
        contrastScreen(showSnackbarSimple) { navController.openRoute(SimulateRoute) }
        contrastGridScreen(showSnackbarSimple)
        paletteBuilderScreen(
            onShowSnackbar = showSnackbarFull,
            onSendToThemeStudio = { hsl ->
                navController.openRoute(ThemeStudioRoute(hsl.hue, hsl.saturation, hsl.lightness))
            },
        )
        gradientScreen(showSnackbarSimple)
        themeStudioScreen(showSnackbarSimple)
        mixLabScreen()
        illusionLabScreen()
        simulateScreen()
        settingsScreen(
            onBack = navController::popBackStack,
            onOpenCrashReports = { navController.openRoute(CrashReportsRoute) },
            onOpenHelp = { navController.openRoute(HelpRoute) },
        )
        crashReportsScreen(onBack = navController::popBackStack)
        helpScreen(onBack = navController::popBackStack)
    }
}
