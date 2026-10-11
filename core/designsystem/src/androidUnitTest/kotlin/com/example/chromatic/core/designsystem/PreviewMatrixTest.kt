package com.example.chromatic.core.designsystem

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.chromatic.core.designsystem.previews.DesignSystemPreviews
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = RobolectricDeviceQualifiers.Pixel7)
class PreviewMatrixTest {

    @get:Rule val compose = createComposeRule()

    @Test fun capturesEveryRegisteredPreview() {
        DesignSystemPreviews.all.forEach { case ->
            compose.setContent { case.content() }
            compose.waitForIdle()
            compose.onRoot().captureRoboImage("src/androidUnitTest/screenshots/previews/${case.id}.png")
        }
    }
}
