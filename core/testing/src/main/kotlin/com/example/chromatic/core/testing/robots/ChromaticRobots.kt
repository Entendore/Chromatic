package com.example.chromatic.core.testing

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.SemanticsActions
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction

/**
 * Screen-object robots (v13 §1). Locale policy: English literals — Robolectric
 * defaults to en-US and device runs pin the locale; translation correctness has
 * its own dedicated smoke test.
 */
fun SemanticsNodeInteractionsProvider.chromatic(block: ChromaticRobots.() -> Unit) =
    ChromaticRobots(this).block()

class ChromaticRobots(private val compose: SemanticsNodeInteractionsProvider) {
    fun nav(block: NavRobot.() -> Unit) = NavRobot(compose).block()
    fun learn(block: LearnRobot.() -> Unit) = LearnRobot(compose).block()
    fun quiz(block: QuizRobot.() -> Unit) = QuizRobot(compose).block()
    fun harmony(block: HarmonyRobot.() -> Unit) = HarmonyRobot(compose).block()
    fun builder(block: BuilderRobot.() -> Unit) = BuilderRobot(compose).block()
}

class NavRobot(private val compose: SemanticsNodeInteractionsProvider) {
    fun openTab(label: String) { compose.onNodeWithText(label).performClick() }
    fun openSettings() { compose.onNodeWithContentDescription("Settings").performClick() }
}

class LearnRobot(private val compose: SemanticsNodeInteractionsProvider) {
    fun assertHome() { compose.onNodeWithText("Your progress").assertIsDisplayed() }
    fun openLesson(title: String) { compose.onNodeWithText(title).performClick() }
    fun tapPractice(label: String) { compose.onNodeWithText(label).performClick() }
}

class QuizRobot(private val compose: SemanticsNodeInteractionsProvider) {
    fun assertQuestion(number: Int) {
        compose.onNodeWithText("Question $number of 6").assertIsDisplayed()
    }
    fun answerFirstOption() { compose.onAllNodesWithTag("quiz.option")[0].performClick() }
    fun assertNextAvailable() { compose.onNodeWithText("Next question").assertIsDisplayed() }
    fun goNext() { compose.onNodeWithText("Next question").performClick() }
}

class HarmonyRobot(private val compose: SemanticsNodeInteractionsProvider) {
    fun setHue(fraction: Float) {
        compose.onNodeWithTag("harmony.slider.hue")
            .performSemanticsAction(SemanticsActions.SetProgress) { it(fraction) }
    }
    fun firstSwatchHex(): String? = compose.onNodeWithTag("harmony.swatch.0")
        .fetchSemanticsNode()
        .config.getOrNull(SemanticsProperties.Text)?.firstOrNull()?.text
}

class BuilderRobot(private val compose: SemanticsNodeInteractionsProvider) {
    fun assertPaletteVisible(name: String) { compose.onNodeWithText(name).assertIsDisplayed() }
    fun delete(name: String) { compose.onNodeWithContentDescription("Delete $name").performClick() }
    fun undo() { compose.onNodeWithText("Undo").performClick() }
}

// Missing androidx.compose.ui.test assertions import guard:
private val unusedAssertGuard: Any = kotlin.run {
    // assertIsDisplayed/assertExists are extension functions resolved at call sites;
    // this guard documents the dependency explicitly.
    Unit
}
