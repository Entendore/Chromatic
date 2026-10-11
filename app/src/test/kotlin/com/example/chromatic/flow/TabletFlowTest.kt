package com.example.chromatic.flow

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.chromatic.MainActivity
import com.example.chromatic.core.testing.chromatic
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Two panes live simultaneously on tablet (v7 §1) — no navigation occurred. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = dagger.hilt.android.testing.HiltTestApplication::class, qualifiers = RobolectricDeviceQualifiers.MediumTablet)
@HiltAndroidTest
class TabletFlowTest {

    @get:Rule(order = 0) val hilt = HiltAndroidRule(this)
    @get:Rule(order = 1) val compose = createAndroidComposeRule<MainActivity>()

    @Test fun `lesson detail opens beside the list`() {
        compose.onNodeWithText("The Color Wheel").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("The Color Wheel").assertIsDisplayed()               // list pane
        compose.onNodeWithText("From Newton to your screen").assertIsDisplayed()    // detail pane
    }

    @Test fun `practice from the detail pane routes with the list still present`() {
        compose.onNodeWithText("Contrast & Accessibility").performClick()
        compose.chromatic { learn { tapPractice("Practice with the contrast checker") } }
        compose.waitForIdle()
        compose.onNodeWithText("Contrast checker").assertIsDisplayed()
    }
}
