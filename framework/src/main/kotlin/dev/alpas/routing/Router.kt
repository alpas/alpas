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
    private val namedRoutes: Map<String, Route> by lazy {
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
        buildRouter(routes, packageClassLoader)
    }

    private fun buildRouter(routes: List<Route>, packageClassLoader: PackageClassLoader) {
        router = BaseRouter.builder<Route>().apply {
            optionalTrailingSlash(true)
            routes.forEach {
                it.build(packageClassLoader)
                route(it.method.name, it.path, it)
            }
        }.build()
    }

    override fun toString(): String {
        val header = """
            | Method 	| URI 	| Name 	| Handler 	|
            |--------	|-----	|------	|--------	|
        """.trimIndent()
        val sb = StringBuilder(header).appendln()
        routes.forEach {
            sb.appendln("| ${it.method} | ${it.path} | ${it.name}| ${it.handler}")
        }
        return sb.toString()
    }

    fun findNamedRoute(name: String): Route? {
        return namedRoutes[name]
    }

    fun print(): Router {
        println(this)
        return this
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
