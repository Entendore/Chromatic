package com.example.chromatic.flow

import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.chromatic.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import org.junit.Rule
import org.junit.Test
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.example.chromatic.core.testing.flow.FlowFakes

/**
 * v13 §0b CORRECTED pattern: JUnit only registers rules declared as annotated
 * FIELDS — so the rules live in this base class and each entry route subclasses it.
 * (v13's first cut built rules inside helpers; those tests never ran.)
 */
abstract class EntryFlowTest(private val launchIntent: Intent) {
    @get:Rule(order = 0) val hilt = HiltAndroidRule(this)
    @get:Rule(order = 1) val compose = createAndroidComposeRule<MainActivity>(launchIntent)
}

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = dagger.hilt.android.testing.HiltTestApplication::class, qualifiers = RobolectricDeviceQualifiers.Pixel7)
@HiltAndroidTest
class DeepLinkHarmonyTest : EntryFlowTest(
    Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
        .apply { data = android.net.Uri.parse("chromatic://harmony") }
) {
    @Test fun `deep link lands on harmony`() {
        compose.onNodeWithText("Harmony explorer").assertIsDisplayed()
        compose.onNodeWithText("Your progress").assertDoesNotExist()
    }
}

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = dagger.hilt.android.testing.HiltTestApplication::class, qualifiers = RobolectricDeviceQualifiers.Pixel7)
@HiltAndroidTest
class DeepLinkContrastTest : EntryFlowTest(
    Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
        .apply { data = android.net.Uri.parse("chromatic://contrast") }
) {
    @Test fun `deep link lands on contrast`() {
        compose.onNodeWithText("Contrast checker").assertIsDisplayed()
    }
}

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = dagger.hilt.android.testing.HiltTestApplication::class, qualifiers = RobolectricDeviceQualifiers.Pixel7)
@HiltAndroidTest
class DeepLinkGridTest : EntryFlowTest(
    Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
        .apply { data = android.net.Uri.parse("chromatic://grid") }
) {
    @Test fun `deep link lands on the contrast grid`() {
        compose.onNodeWithText("Contrast grid").assertIsDisplayed()
    }
}

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = dagger.hilt.android.testing.HiltTestApplication::class, qualifiers = RobolectricDeviceQualifiers.Pixel7)
@HiltAndroidTest
class ShortcutContrastTest : EntryFlowTest(
    Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
        .apply { putExtra(MainActivity.EXTRA_ROUTE, "contrast") }
) {
    @Test fun `shortcut extra lands directly on contrast`() {
        compose.onNodeWithText("Contrast checker").assertIsDisplayed()
        compose.onNodeWithText("Your progress").assertDoesNotExist()
    }
}

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = dagger.hilt.android.testing.HiltTestApplication::class, qualifiers = RobolectricDeviceQualifiers.Pixel7)
@HiltAndroidTest
class DeepLinkLessonTest : EntryFlowTest(
    Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
        .apply { data = android.net.Uri.parse("chromatic://lesson/6") }
) {
    @Test fun `lesson deep link opens the lesson directly (v16 §1)`() {
        compose.onNodeWithText("How contrast is measured").assertIsDisplayed()
    }

    @Test fun `pane back returns to the list, not a dead entry (v17 §0a regression)`() {
        compose.activityRule.scenario.onActivity { activity ->
            activity.onBackPressedDispatcher.onBackPressed()
        }
        compose.waitForIdle()
        compose.onNodeWithText("Contrast & Accessibility").assertIsDisplayed()
        compose.onNodeWithText("How contrast is measured").assertDoesNotExist()
    }
}
