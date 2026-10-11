package com.example.chromatic.screenshots

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.chromatic.MainActivity
import com.example.chromatic.core.testing.chromatic
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy
import tools.fastlane.screengrab.junit.ScreengrabTestRule

/** Marketing pixels from the same hermetic fixtures as the tests (v18 §5). */
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class StoreScreenshotsTest {

    @get:Rule(order = 0) val hilt = HiltAndroidRule(this)
    @get:Rule(order = 1) val compose = createAndroidComposeRule<MainActivity>()
    @get:Rule(order = 2) val screengrabRule = ScreengrabTestRule()

    init { Screengrab.setDefaultScreenshotStrategy(UiAutomatorScreenshotStrategy()) }

    @Test fun captureListingShots() {
        compose.waitForIdle()
        Screengrab.screenshot("01_home")
        compose.chromatic { nav { openTab("Harmony") } }
        compose.chromatic { harmony { setHue(0.62f) } }
        compose.waitForIdle()
        Screengrab.screenshot("02_harmony")
        compose.chromatic { nav { openTab("Contrast") } }
        compose.waitForIdle()
        Screengrab.screenshot("03_contrast")
        compose.chromatic { nav { openTab("Build") } }
        compose.waitForIdle()
        Screengrab.screenshot("04_builder")
        compose.chromatic { nav { openTab("Quiz") } }
        compose.waitForIdle()
        Screengrab.screenshot("05_quiz")
    }
}
