package dev.alpas.routing

import dev.alpas.Middleware
import dev.alpas.auth.middleware.AuthOnlyMiddleware
import dev.alpas.auth.middleware.GuestOnlyMiddleware
import dev.alpas.auth.middleware.VerifiedEmailOnlyMiddleware
import dev.alpas.http.HttpCall
import dev.alpas.http.Method
import dev.alpas.routing.middleware.SignedRequestMiddleware
import kotlin.reflect.KClass

class RouteGroup(
    private val prefix: String,
    val middleware: MutableSet<KClass<out Middleware<HttpCall>>> = mutableSetOf(),
    val middlewareGroups: MutableSet<String> = mutableSetOf()
) : RoutableBase(middleware, middlewareGroups) {
    internal var name = ""
        private set
    internal var skipCsrfCheck = false
        private set

    override fun add(
        method: Method,
        path: String,
        handler: RouteHandler,
        middleware: Set<KClass<out Middleware<HttpCall>>>
    ): Route {
        return super.add(method, path, handler, middleware).also {
            it.groupName = name
        }
    }

    fun name(name: String): RouteGroup {
        if (this.name.isNotBlank()) return this
        this.name = name
        routes.forEach { route -> route.groupName = fullName(route.groupName) }
        return this
    }

    fun middleware(
        middleware: KClass<out Middleware<HttpCall>>,
        vararg others: KClass<out Middleware<HttpCall>>
    ): RouteGroup {
        this.middleware.add(middleware)
        this.middleware.addAll(others)
        routes.forEach { route -> route.middleware(middleware, *others) }
        return this
    }

    fun skipCsrfCheck(): RouteGroup {
        skipCsrfCheck = true
        routes.forEach { it.skipCsrfCheck() }
        return this
    }

    fun mustBeAuthenticated(authChannel: String? = null): RouteGroup {
        middleware(AuthOnlyMiddleware::class as KClass<out Middleware<HttpCall>>)
        routes.forEach { route ->
            if (route.authChannel == null) {
                route.authChannel = authChannel
            }
        }
        return this
    }

    fun mustBeGuest(): RouteGroup {
        middleware(GuestOnlyMiddleware::class as KClass<out Middleware<HttpCall>>)
        return this
    }

    fun mustBeSigned(): RouteGroup {
        middleware(SignedRequestMiddleware::class as KClass<out Middleware<HttpCall>>)
        return this
    }

    fun mustBeVerified(): RouteGroup {
        middleware(VerifiedEmailOnlyMiddleware::class as KClass<out Middleware<HttpCall>>)
        return this
    }

    fun middlewareGroup(groupName: String): RouteGroup {
        middlewareGroups.add(groupName)
        routes.forEach { route -> route.middlewareGroup(groupName) }
        return this
    }

    fun middlewareGroup(groupNames: List<String>): RouteGroup {
        middlewareGroups.addAll(groupNames)
        routes.forEach { route -> route.middlewareGroup(groupNames) }
        return this
    }

    override fun fullName(name: String) = combineNames(this.name, name)

    override fun fullPath(path: String) = combinePaths(this.prefix, path)
}
