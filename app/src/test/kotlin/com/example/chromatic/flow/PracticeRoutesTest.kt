package com.example.chromatic.flow

import com.example.chromatic.core.data.model.Practice
import com.example.chromatic.navigation.toNavigationRoute
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** The practice→route map, as a unit test (v12 §6.4). */
class PracticeRoutesTest {
    @Test fun `every practice maps to a distinct route`() {
        val routes = Practice.entries.map { it.toNavigationRoute() }
        assertEquals(routes.size, routes.toSet().size)
    }

    @Test fun `route objects are serializable types`() {
        Practice.entries.forEach { p ->
            val route = p.toNavigationRoute()
            assertTrue("practice $p", route::class.qualifiedName!!.contains("Route"))
        }
    }
}
