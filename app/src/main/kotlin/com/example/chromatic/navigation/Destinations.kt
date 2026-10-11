package com.example.chromatic.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.School
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.example.chromatic.core.data.model.Practice
import com.example.chromatic.feature.learn.LearnRoute
import com.example.chromatic.feature.quiz.QuizRoute
import com.example.chromatic.feature.tools.ContrastGridRoute
import com.example.chromatic.feature.tools.ContrastRoute
import com.example.chromatic.feature.tools.HarmonyRoute
import com.example.chromatic.feature.tools.IllusionLabRoute
import com.example.chromatic.feature.tools.MixLabRoute
import com.example.chromatic.feature.tools.PaletteRoute
import com.example.chromatic.feature.tools.SimulateRoute
import com.example.chromatic.feature.tools.ThemeStudioRoute

enum class TopLevelDestination(
    val route: Any,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    LEARN(LearnRoute, Icons.Filled.School, Icons.Outlined.School),
    QUIZ(QuizRoute, Icons.Filled.Quiz, Icons.Outlined.Quiz),
    HARMONY(HarmonyRoute, Icons.Filled.Palette, Icons.Outlined.Palette),
    CONTRAST(ContrastRoute, Icons.Filled.Contrast, Icons.Outlined.Contrast),
    BUILDER(PaletteRoute, Icons.Filled.ColorLens, Icons.Outlined.ColorLens),
}

@Composable
fun TopLevelDestination.label(): String = stringResource(when (this) {
    TopLevelDestination.LEARN -> R.string.nav_learn
    TopLevelDestination.QUIZ -> R.string.nav_quiz
    TopLevelDestination.HARMONY -> R.string.nav_harmony
    TopLevelDestination.CONTRAST -> R.string.nav_contrast
    TopLevelDestination.BUILDER -> R.string.nav_build
})

fun Practice.toNavigationRoute(): Any = when (this) {
    Practice.HARMONY -> HarmonyRoute
    Practice.CONTRAST -> ContrastRoute
    Practice.PALETTE -> PaletteRoute
    Practice.GRID -> ContrastGridRoute
    Practice.ILLUSION -> IllusionLabRoute
    Practice.MIX -> MixLabRoute
    Practice.THEME -> ThemeStudioRoute()
}

/** Nonce'd so repeated taps of the SAME shortcut still navigate (v5 §5). */
data class ShortcutRequest(val route: Any, val nonce: Long)
