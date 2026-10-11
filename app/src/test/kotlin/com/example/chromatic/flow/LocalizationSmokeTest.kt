package com.example.chromatic.flow

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.chromatic.MainActivity
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.qualifiers

/** The ONE flow test allowed to assert translations (v13 §1 locale policy). */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = dagger.hilt.android.testing.HiltTestApplication::class, qualifiers = "es")
@HiltAndroidTest
class LocalizationSmokeTest {

    @get:Rule(order = 0) val hilt = HiltAndroidRule(this)
    @get:Rule(order = 1) val compose = createAndroidComposeRule<MainActivity>()

    @Test fun `spanish renders on home`() {
        compose.onNodeWithText("Tu progreso").assertIsDisplayed()
    }
}
