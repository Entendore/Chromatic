package com.example.chromatic.feature.tools

import com.example.chromatic.core.color.BlendSpace
import com.example.chromatic.feature.tools.gradient.GradientViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class GradientViewModelTest {

    @Test fun `mid-edit invalid hex preserves the ramp (v12 §4 + v9 memoization)`() = runTest {
        val vm = GradientViewModel()
        val before = vm.uiState.value.stops
        vm.onToChange("#F76B")                       // too short — invalid
        assertEquals(before, vm.uiState.value.stops) // last valid ramp kept
        vm.onToChange("#F76B8A")
        assertNotEquals(before, vm.uiState.value.stops)
    }

    @Test fun `swap exchanges endpoints`() = runTest {
        val vm = GradientViewModel()
        val first = vm.uiState.value.from
        vm.onSwap()
        assertEquals(first, vm.uiState.value.to)
    }

    @Test fun `space change recalculates the ramp`() = runTest {
        val vm = GradientViewModel()
        val srgb = vm.uiState.value.stops.toList()
        vm.onSpaceChange(BlendSpace.LINEAR_SRGB)
        assertNotEquals(srgb, vm.uiState.value.stops)
    }
}
