package com.example.chromatic.feature.learn

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable

@Serializable data object LearnRoute
@Serializable data class LessonDeepLinkRoute(val lessonId: Long)

fun NavHostController.navigateToTopLevel(route: Any) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

fun NavGraphBuilder.learnScreen(
    onOpenQuiz: () -> Unit,
    onOpenVision: () -> Unit,
    onOpenGradient: () -> Unit,
    onOpenThemeStudio: () -> Unit,
    onOpenMixLab: () -> Unit,
    onOpenIllusionLab: () -> Unit,
    onOpenGrid: () -> Unit,
    onOpenSettings: () -> Unit,
    onPractice: (com.example.chromatic.core.data.model.Practice) -> Unit,
) {
    composable<LearnRoute> {
        LearnScreen(
            onOpenQuiz, onOpenVision, onOpenGradient, onOpenThemeStudio,
            onOpenMixLab, onOpenIllusionLab, onOpenGrid, onOpenSettings, onPractice,
        )
    }
    composable<LessonDeepLinkRoute>(
        deepLinks = listOf(navDeepLink<LessonDeepLinkRoute>(uriPattern = "chromatic://lesson/{lessonId}")),
    ) { entry ->
        val id = entry.toRoute<LessonDeepLinkRoute>().lessonId
        LearnScreen(
            onOpenQuiz, onOpenVision, onOpenGradient, onOpenThemeStudio,
            onOpenMixLab, onOpenIllusionLab, onOpenGrid, onOpenSettings, onPractice,
            pendingLessonId = id,
        )
    }
}
