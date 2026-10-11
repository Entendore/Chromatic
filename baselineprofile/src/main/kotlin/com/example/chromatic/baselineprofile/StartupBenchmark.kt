package com.example.chromatic.baselineprofile

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StartupBenchmark {

    @get:Rule val benchmarkRule = MacrobenchmarkRule()

    @Test fun startupCompilationNone() = startup(CompilationMode.None())
    @Test fun startupWithBaselineProfile() =
        startup(CompilationMode.Partial(baselineProfileMode = BaselineProfileMode.Require))

    @Test fun navigateToHarmony() = benchmarkRule.measureRepeated(
        packageName = TARGET_PACKAGE,
        metrics = listOf(StartupTimingMetric()),
        iterations = 10,
        startupMode = StartupMode.COLD,
    ) {
        pressHome()
        startActivityAndWait()
        device.findObject(By.text("Harmony")).click()
        device.wait(Until.hasObject(By.textContains("Lightness")), 2_000)
        device.pressBack()
    }

    /** v8 §7: jank gate on the heaviest screen — percentiles become nightly trend artifacts. */
    @Test fun scrollContrastGrid_noJankRegression() = benchmarkRule.measureRepeated(
        packageName = TARGET_PACKAGE,
        metrics = listOf(FrameTimingMetric()),
        iterations = 5,
        startupMode = StartupMode.WARM,
        setupBlock = {
            pressHome()
            startActivityAndWait()
            device.findObject(By.text("Contrast")).click()
            device.wait(Until.hasObject(By.textContains("pairings pass")), 5_000)
        },
    ) {
        device.swipe(
            device.displayWidth / 2, device.displayHeight * 3 / 4,
            device.displayWidth / 2, device.displayHeight / 4, 20,
        )
        device.waitForIdle()
    }

    private fun startup(mode: CompilationMode) = benchmarkRule.measureRepeated(
        packageName = TARGET_PACKAGE,
        metrics = listOf(StartupTimingMetric()),
        compilationMode = mode,
        iterations = 10,
        startupMode = StartupMode.COLD,
    ) {
        pressHome()
        startActivityAndWait()
    }

    companion object { const val TARGET_PACKAGE = "com.example.chromatic" }
}
