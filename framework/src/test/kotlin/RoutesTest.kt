package dev.alpas.tests

import dev.alpas.Middleware
import dev.alpas.auth.middleware.AuthOnlyMiddleware
import dev.alpas.auth.middleware.GuestOnlyMiddleware
import dev.alpas.auth.middleware.VerifiedEmailOnlyMiddleware
import dev.alpas.http.HttpCall
import dev.alpas.http.Method
import dev.alpas.routing.Controller
import dev.alpas.routing.ControllerHandler
import dev.alpas.routing.dynamicControllerFactory
import dev.alpas.routing.middleware.SignedRequestMiddleware
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RoutesTest {
    @Test
    fun `route can be given a name`() {
        val getRoutes = mapOf("/get_test" to "get_test_route", "/get_test1" to "get_test1_route")
        val postRoutes = mapOf("/post_test" to "post_test_route", "/post_test1" to "post_test1_route")
        val patchRoutes = mapOf("/patch_test" to "patch_test_route", "/patch_test1" to "patch_test1_route")
        val deleteRoutes = mapOf("/delete_test" to "delete_test_route", "/delete_test1" to "delete_test1_route")
        val allRoutes =
            mapOf("get" to getRoutes, "post" to postRoutes, "patch" to patchRoutes, "delete" to deleteRoutes)

        val router = withRouter {
            allRoutes.forEach { (method, routes) ->
                when (method) {
                    "get" -> {
                        routes.forEach { (path, name) ->
                            get(path) {}.name(name)
                        }
                    }
                    "post" -> {
                        routes.forEach { (path, name) ->
                            post(path) {}.name(name)
                        }
                    }
                    "patch" -> {
                        routes.forEach { (path, name) ->
                            patch(path) {}.name(name)
                        }
                    }
                    "delete" -> {
                        routes.forEach { (path, name) ->
                            delete(path) {}.name(name)
                        }
                    }
                }
            }
        }

        val combinedRoutes = allRoutes.values.reduce { acc, map -> acc + map }
        combinedRoutes.forEach { route ->
            with(router.findNamedRoute(route.value)) {
                assertNotNull(this)
                assertEquals(route.key, this?.path)
                assertEquals(route.value, this?.name)
            }
        }
    }

    @Test
    fun `parent route group names are prepended with a route's name`() {
        val routePaths = mapOf("path_one" to "one", "path_two" to "two", "path_three" to "three", "path_four" to "four")
        val router = withRouter {
            group {
                group {
                    routePaths.forEach {
                        get(it.key) {}.name("get_${it.value}")
                        post(it.key) {}.name("post_${it.value}")
                        patch(it.key) {}.name("patch_${it.value}")
                        delete(it.key) {}.name("delete_${it.value}")
                    }
                }.name("group")
            }.name("test")
        }

        routePaths.forEach { routePath ->
            listOf("get", "post", "patch", "delete").forEach { method ->
                router.findNamedRoute("test.group.${method}_${routePath.value}").also { route ->
                    assertNotNull(route)
                    assertEquals("/${routePath.key}", route?.path)
                }
            }
        }
    }

    @Test
    fun `parent route group paths are prepended with a route's path`() {
        val routePaths = listOf("path_one", "path_two", "path_three", "path_four")
        val router = withRouter {
            group("test") {
                group("group") {
                    routePaths.forEach { path ->
                        get(path) {}
                        post(path) {}
                        patch(path) {}
                        delete(path) {}
                    }
                }
            }
        }

        routePaths.forEach { path ->
            router.routes.find { route ->
                route.path == "/test/group/${path}"
            }.also {
                assertNotNull(path)
            }
        }
    }

    @Test
    fun `path is set to default when no path is provided`() {
        val router = withRouter {
            get() {}
            post() {}
            patch() {}
            delete() {}
        }
        listOf(
            Method.GET,
            Method.POST,
            Method.PATCH,
            Method.DELETE
        ).forEach { method ->
            val route = router.routes.find { route -> route.method == method }
            assertEquals("/", route?.path)
        }
    }

    @Test
    fun `method name is not conventionally derived for a dynamic controller route`() {
        val router = withRouter {
            dynamicControllerFactory = { TestController() }
            val controller = "TestController"
            get("/", controller, "index")
            post("/", controller, "store")
            patch("/", controller, "update")
            delete("/", controller, "delete")
        }
        mapOf(
            Method.GET to "index",
            Method.POST to "store",
            Method.PATCH to "update",
            Method.DELETE to "delete"
        ).forEach { methodEntry ->
            val route = router.routes.find { route -> route.method == methodEntry.key }
            assertEquals(methodEntry.value, route?.controllerMethod())
        }
    }

    @Test
    fun `method name is conventionally derived for a strongly typed controller route`() {
        val router = withRouter {
            get("/", TestController::class)
            post("/", TestController::class)
            patch("/", TestController::class)
            delete("/", TestController::class)
        }
        mapOf(
            Method.GET to "index",
            Method.POST to "store",
            Method.PATCH to "update",
            Method.DELETE to "delete"
        ).forEach { methodEntry ->
            val route = router.routes.find { route -> route.method == methodEntry.key }
            assertEquals(methodEntry.value, route?.controllerMethod())
        }
    }

    @Test
    fun `mustBeAuthenticated() method adds proper middleware`() {
        val router = withRouter {
            get("/no-auth", TestController::class)
            get("/", TestController::class).mustBeAuthenticated()
            group("auth") {
                get("/", TestController::class)
                post("/", TestController::class)
                patch("/", TestController::class)
                delete("/", TestController::class)
            }.mustBeAuthenticated()
        }

        assertFalse(
            router.routes.find { it.path == "/no-auth" }?.middleware?.contains(AuthOnlyMiddleware::class) ?: true
        )
        val authRoutes = router.routes.filter { it.path != "/no-auth" }
        assertEquals(5, authRoutes.size)
        authRoutes.forEach { route -> assertTrue(route.middleware.contains(AuthOnlyMiddleware::class)) }
    }

    @Test
    fun `mustBeGuest() method adds proper middleware`() {
        val router = withRouter {
            get("/no-guest", TestController::class)
            get("/", TestController::class).mustBeGuest()
            group("guest") {
                get("/", TestController::class)
                post("/", TestController::class)
                patch("/", TestController::class)
                delete("/", TestController::class)
            }.mustBeGuest()
        }

        assertFalse(
            router.routes.find { it.path == "/no-guest" }?.middleware?.contains(GuestOnlyMiddleware::class) ?: true
        )
        val guestRoutes = router.routes.filter { it.path != "/no-guest" }
        assertEquals(5, guestRoutes.size)
        guestRoutes.forEach { route -> assertTrue(route.middleware.contains(GuestOnlyMiddleware::class)) }
    }

    @Test
    fun `mustBeVerified() method adds proper middleware`() {
        val router = withRouter {
            get("/no-verified", TestController::class)
            get("/", TestController::class).mustBeVerified()
            group("verified") {
                get("/", TestController::class)
                post("/", TestController::class)
                patch("/", TestController::class)
                delete("/", TestController::class)
            }.mustBeVerified()
        }

        assertFalse(
            router.routes.find { it.path == "/no-verified" }?.middleware?.contains(VerifiedEmailOnlyMiddleware::class)
                ?: true
        )
        val verifiedRoutes = router.routes.filter { it.path != "/no-verified" }
        assertEquals(5, verifiedRoutes.size)
        verifiedRoutes.forEach { route -> assertTrue(route.middleware.contains(VerifiedEmailOnlyMiddleware::class)) }
    }

    @Test
    fun `mustBeSigned() method adds proper middleware`() {
        val router = withRouter {
            get("/no-signed", TestController::class)
            get("/", TestController::class).mustBeSigned()
            group("signed") {
                get("/", TestController::class)
                post("/", TestController::class)
                patch("/", TestController::class)
                delete("/", TestController::class)
            }.mustBeSigned()
        }

        assertFalse(
            router.routes.find { it.path == "/no-signed" }?.middleware?.contains(SignedRequestMiddleware::class)
                ?: true
        )
        val signedRoutes = router.routes.filter { it.path != "/no-signed" }
        assertEquals(5, signedRoutes.size)
        signedRoutes.forEach { route -> assertTrue(route.middleware.contains(SignedRequestMiddleware::class)) }
    }


    @Test
    fun `can assign an auth channel name`() {
        val router = withRouter {
            get("/", TestController::class).mustBeAuthenticated("test-channel")
            group("auth") {
                get("/", TestController::class)
                post("/", TestController::class)
                patch("/", TestController::class)
                delete("/", TestController::class)
            }.mustBeAuthenticated("test-channel")
        }

        router.routes.forEach { route -> assertEquals("test-channel", route.authChannel) }
    }

    @Test
    fun `can add middleware`() {
        class TestMiddleware1 : Middleware<HttpCall>()
        class TestMiddleware2 : Middleware<HttpCall>()
        class TestMiddleware3 : Middleware<HttpCall>()

        val router = withRouter {
            get("/", TestController::class).middleware(
                TestMiddleware3::class,
                TestMiddleware1::class,
                TestMiddleware2::class
            )
            group(middleware = TestMiddleware3::class) {
                get("/", TestController::class)
                post("/", TestController::class)
                patch("/", TestController::class)
                delete("/", TestController::class)
            }.middleware(TestMiddleware1::class, TestMiddleware2::class)
        }

        router.routes.forEach { route ->
            route.middleware.containsAll(
                listOf(
                    TestMiddleware1::class,
                    TestMiddleware2::class,
                    TestMiddleware3::class
                )
            )
        }
    }

    @Test
    fun `can use strongly typed methods without specifying the controller's name`() {
        val router = withRouter {
            get(TypedController::index)
            post(TypedController::store)
            patch(TypedController::update)
            delete(TypedController::delete)
        }
        mapOf(
            Method.GET to "index",
            Method.POST to "store",
            Method.PATCH to "update",
            Method.DELETE to "delete"
        ).forEach { (method, name) ->
            val route = router.routes.find { it.method == method }
            assertNotNull(route)
            assertEquals(name, route?.controllerMethod())
            assertEquals("/", route?.path)
            assertEquals(
                "dev.alpas.tests.TypedController",
                (route?.handler as ControllerHandler)?.controller.qualifiedName
            )
        }
    }

    @Test
    fun `can use generic controller with conventional names`() {
        val router = withRouter {
            get<TypedController>()
            post<TypedController>()
            patch<TypedController>()
            delete<TypedController>()
        }
        mapOf(
            Method.GET to "index",
            Method.POST to "store",
            Method.PATCH to "update",
            Method.DELETE to "delete"
        ).forEach { (method, name) ->
            val route = router.routes.find { it.method == method }
            assertNotNull(route)
            assertEquals(name, route?.controllerMethod())
            assertEquals("/", route?.path)
            assertEquals(
                "dev.alpas.tests.TypedController",
                (route?.handler as ControllerHandler)?.controller.qualifiedName
            )
        }
    }

    @Test
    fun `can override paths for strongly typed controller methods`() {
        val router = withRouter {
            get("/get", TypedController::index)
            post("/post", TypedController::store)
            patch("/patch", TypedController::update)
            delete("/delete", TypedController::delete)
        }
        listOf(
            Method.GET,
            Method.POST,
            Method.PATCH,
            Method.DELETE
        ).forEach { method ->
            val route = router.routes.find { it.method == method }
            assertNotNull(route)
            assertEquals("${route?.path?.toLowerCase()}", route?.path)
        }
    }

    @Test
    fun `can use dynamic method names for strongly typed controllers`() {
        val router = withRouter {
            get<TypedController>("", "testget")
            post<TypedController>("", "testpost")
            patch<TypedController>("", "testpatch")
            delete<TypedController>("", "testdelete")
        }
        listOf(
            Method.GET,
            Method.POST,
            Method.PATCH,
            Method.DELETE
        ).forEach { method ->
            val route = router.routes.find { it.method == method }
            assertNotNull(route)
            assertEquals("${route?.path?.toLowerCase()}", route?.path)
            assertEquals("test${method.toString().toLowerCase()}", (route?.handler as ControllerHandler).method)
            assertEquals(
                "dev.alpas.tests.TypedController",
                (route?.handler as ControllerHandler)?.controller.qualifiedName
            )
        }
    }
}

class TypedController : Controller() {
    fun index(call: HttpCall) {}
    fun store(call: HttpCall) {}
    fun update(call: HttpCall) {}
    fun delete(call: HttpCall) {}
}
