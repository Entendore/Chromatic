package com.example.chromatic.feature.quiz

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable data object QuizRoute

fun NavGraphBuilder.quizScreen() {
    composable<QuizRoute> { QuizScreen() }
}
