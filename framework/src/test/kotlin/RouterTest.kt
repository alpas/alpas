package dev.alpas.tests

import dev.alpas.http.Method
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RouterTest {
    @Test
    fun `can add routes`() {
        val router = withRouter {
            get("/") {}
            get("/test") {}
            post("/test") {}
        }

        val routes = router.routes
        assertEquals(3, routes.size)
        assertNotNull(routes.find { it.path == "/" && it.method == Method.GET })
        assertNotNull(routes.find { it.path == "/test" && it.method == Method.GET })
        assertNotNull(routes.find { it.path == "/test" && it.method == Method.POST })
    }

    @Test
    fun `can match a route`() {
        val router = withRouter {
            get("/") {}.name("home")
            get("/hello/world") {}.name("home.test")
            post("/test") {}.name("home.test.create")
            patch("/test/<id>") {}.name("home.test.update")
        }
        router.routeFor("get", "/").apply {
            assertTrue(isSuccess)
            assertEquals("home", target().name)
        }
        router.routeFor("get", "/hello/world").apply {
            assertTrue(isSuccess)
            assertEquals("home.test", target().name)
        }
        router.routeFor("post", "/test").apply {
            assertTrue(isSuccess)
            assertEquals("home.test.create", target().name)
        }
        router.routeFor("patch", "/test/5").apply {
            assertTrue(isSuccess)
            assertEquals(1, params())
            assertEquals("id", paramName(0))
            assertEquals("5", paramValue(0))
            assertEquals("home.test.update", target().name)
        }

        router.routeFor("delete", "/test").apply {
            assertFalse(isSuccess)
        }
    }
}
