package dev.alpas.routing

import dev.alpas.Middleware
import dev.alpas.PackageClassLoader
import dev.alpas.http.HttpCall
import kotlin.reflect.KClass
import io.norberg.rut.Router as BaseRouter

typealias RouteResult = io.norberg.rut.Router.Result<Route>
typealias RouteMatchStatus = io.norberg.rut.Router.Status

class Router(
    middleware: Set<KClass<Middleware<HttpCall>>> = emptySet(),
    middlewareGroups: Set<String> = emptySet()
) : RoutableBase(middleware, middlewareGroups) {

    private lateinit var router: BaseRouter<Route>
    val namedRoutes: Map<String, Route> by lazy {
        routes.filter { it.name.isNotBlank() }.map { Pair(it.name, it) }.toMap()
    }

    constructor(
        middleware: Set<KClass<Middleware<HttpCall>>> = emptySet(),
        middlewareGroups: Set<String> = emptySet(),
        block: Router.() -> Unit
    ) : this(middleware, middlewareGroups) {
        block()
    }

    constructor(vararg middleware: KClass<Middleware<HttpCall>>) : this(middleware.toSet())

    internal fun routeFor(methodName: String, uri: String): RouteResult {
        return router.result().also {
            router.route(methodName.toUpperCase(), uri, it)
        }
    }

    internal fun compile(packageClassLoader: PackageClassLoader) {
        if (::router.isInitialized) {
            return
        }
        buildRouter(packageClassLoader)
    }

    internal fun forceCompile(packageClassLoader: PackageClassLoader) {
        buildRouter(packageClassLoader)
    }

    private fun buildRouter(packageClassLoader: PackageClassLoader) {
        router = BaseRouter.builder<Route>().also { baseRouter ->
            baseRouter.optionalTrailingSlash(true)
            routes.forEach { route ->
                route.build(packageClassLoader)
                baseRouter.route(route.method.name, route.path, route)
            }
        }.build()
    }

    fun findNamedRoute(name: String): Route? {
        return namedRoutes[name]
    }

    internal fun clear() {
        routes.clear()
    }
}

fun Router.mount(prefix: String, other: Router): Router {
    other.routes.forEach { route ->
        route.path = combinePaths(prefix, route.path)
        routes.add(route)
    }
    return this
}

fun Router.mount(other: Router): Router {
    return mount("/", other)
}

abstract class BaseRouteLoader {
    fun load(router: Router) {
        router.clear()
        add(router)
    }

    protected abstract fun add(router: Router)
}

