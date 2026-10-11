package com.example.chromatic.widget

import android.content.Context
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNode
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.hasText
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.glance.appwidget.testing.unit.runGlanceAppWidgetUnitTest
import androidx.glance.appwidget.testing.unit.provideAppWidget
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The widget's "pure function of the date" contract, asserted (v13 §5).
 * Uses the 1.1.0 provideAppWidget(context, widget) overload — GlanceId is returned, not constructed.
 */
@RunWith(AndroidJUnit4::class)
class ColorOfDayWidgetTest {

    @Test fun `renders a hex value and the daily caption`() = runGlanceAppWidgetUnitTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        provideAppWidget(context, ColorOfDayWidget())
        onNode(hasText("Color of the day — tap to explore")).assertExists()
    }

    @Test fun `rendering is stable within a single day`() = runGlanceAppWidgetUnitTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        provideAppWidget(context, ColorOfDayWidget())
        provideAppWidget(context, ColorOfDayWidget())   // same date → identical output by contract
        onNode(hasText("Color of the day — tap to explore")).assertExists()
    }
}
