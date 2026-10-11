package com.example.chromatic.feature.tools

import com.example.chromatic.core.color.ColorHarmony
import com.example.chromatic.core.color.HarmonyMode
import com.example.chromatic.feature.tools.harmony.HarmonyViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class HarmonyViewModelTest {

    @Test fun `every palette mutation invalidates the cvd analysis (v6 gate, test-locked)`() = runTest {
        val vm = HarmonyViewModel()
        vm.onToggleCvdSafe()
        assertNotNull(vm.cvdResult.value)

        vm.onRandomColor(); assertNull(vm.cvdResult.value)
        vm.onToggleCvdSafe()
        vm.onHarmonySelected(ColorHarmony.TRIADIC); assertNull(vm.cvdResult.value)
        vm.onToggleCvdSafe()
        vm.onModeSelected(HarmonyMode.OKLCH); assertNull(vm.cvdResult.value)
        vm.onToggleCvdSafe()
        vm.onPaletteInput(100f, 0.5f, 0.8f); assertNull(vm.cvdResult.value)
    }

    @Test fun `palette follows the selected mode`() = runTest {
        val vm = HarmonyViewModel()
        vm.onModeSelected(HarmonyMode.OKLCH)
        val oklch = vm.uiState.value.palette
        vm.onModeSelected(HarmonyMode.TRADITIONAL)
        assertNotEquals(oklch, vm.uiState.value.palette)
    }

    @Test fun `drag input is clamped to the valid lightness range`() = runTest {
        val vm = HarmonyViewModel()
        vm.onPaletteInput(120f, 0.5f, 0.05f)
        assertEquals(0.1f, vm.uiState.value.baseHsv.value, 1e-4f)
    }
}
