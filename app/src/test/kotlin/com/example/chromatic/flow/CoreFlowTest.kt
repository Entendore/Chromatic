package com.example.chromatic.flow

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.chromatic.MainActivity
import com.example.chromatic.core.testing.chromatic
import com.example.chromatic.core.testing.fakes
import com.example.chromatic.core.testing.flow.FlowTestBindings
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers

/**
 * Hermetic app-flow tests (v12 §6): real MainActivity + real NavHost, DI swapped
 * for :core:testing fakes, robots for all finders (v13 §1). Locale: en-US by
 * Robolectric default — translation correctness has its own smoke test.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = dagger.hilt.android.testing.HiltTestApplication::class, qualifiers = RobolectricDeviceQualifiers.Pixel7)
@HiltAndroidTest
class CoreFlowTest {

    @get:Rule(order = 0) val hilt = HiltAndroidRule(this)
    @get:Rule(order = 1) val compose = createAndroidComposeRule<MainActivity>()

    @Inject lateinit var flowFakes: com.example.chromatic.core.testing.flow.FlowFakes

    @Before fun setUp() {
        hilt.inject()
        // Confirm the @TestInstallIn graph replaced the production bindings:
        check(flowFakes.lessons.current.isNotEmpty()) { "FlowFakes must be bound for flow tests" }
    }

    private val fakes get() = flowFakes

    @Test fun `learn is the start destination`() {
        compose.onNodeWithText("Your progress").assertIsDisplayed()
        compose.onNodeWithText("Contrast & Accessibility").assertIsDisplayed()
    }

    @Test fun `lesson to practice chip routes to the contrast checker`() {
        compose.chromatic {
            learn {
                assertHome()
                openLesson("Contrast & Accessibility")
                tapPractice("Practice with the contrast checker")
            }
        }
        compose.onNodeWithText("Contrast checker").assertIsDisplayed()
    }

    @Test fun `second practice chip routes to the contrast grid`() {
        compose.chromatic {
            learn {
                openLesson("Contrast & Accessibility")
                tapPractice("Explore the contrast grid")
            }
        }
        compose.onNodeWithText("Contrast grid").assertIsDisplayed()
    }

    @Test fun `top level tabs navigate and content returns`() {
        compose.chromatic { nav { openTab("Harmony") } }
        compose.onNodeWithText("Harmony explorer").assertIsDisplayed()
        compose.chromatic { nav { openTab("Learn") } }
        compose.onNodeWithText("Contrast & Accessibility").assertIsDisplayed()
    }

    @Test fun `quiz round advances through questions (v13 §0a tagged options)`() {
        compose.chromatic {
            nav { openTab("Quiz") }
            quiz {
                assertQuestion(1)
                answerFirstOption()
                assertNextAvailable()
                goNext()
                assertQuestion(2)
            }
        }
    }

    @Test fun `hue slider drives the generated palette (end to end)`() {
        compose.chromatic {
            nav { openTab("Harmony") }
            harmony {
                val before = firstSwatchHex()
                setHue(0.75f)
                compose.waitForIdle()
                val after = firstSwatchHex()
                check(before != after) { "slider input must regenerate the palette" }
            }
        }
    }

    @Test fun `undo delete restores the palette (storage-level verification)`() = runTest {
        compose.chromatic { nav { openTab("Build") } }
        compose.waitForIdle()
        compose.onNodeWithText("Sample Palette").assertIsDisplayed()
        compose.chromatic { builder { delete("Sample Palette") } }
        compose.waitForIdle()
        compose.onNodeWithText("Sample Palette").assertDoesNotExist()
        compose.chromatic { builder { undo() } }
        compose.waitForIdle()
        compose.onNodeWithText("Sample Palette").assertIsDisplayed()
        assertEquals(listOf("flow-1"), fakes.palettes.deleted)
        assertEquals(1, fakes.palettes.saved.size)
    }

    @Test fun `crash diagnostics reachable, populated, and clearable (v17 §7)`() {
        fakes.crashStore.recordSync(
            com.example.chromatic.core.data.crash.CrashReport(
                1_000L, "1.0.0", 1, "main",
                "java.lang.IllegalStateException", "boom\n  at App.onCrash(App.kt:1)", "14", "Pixel 7",
            )
        )
        compose.chromatic { nav { openSettings() } }
        compose.onNodeWithText("Crash reports").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("java.lang.IllegalStateException — main").assertIsDisplayed()
        compose.onNodeWithText("Clear all").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("java.lang.IllegalStateException — main").assertDoesNotExist()
    }
}
