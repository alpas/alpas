package dev.alpas.routing

import dev.alpas.Middleware
import dev.alpas.PackageClassLoader
import dev.alpas.auth.middleware.AuthOnlyMiddleware
import dev.alpas.auth.middleware.GuestOnlyMiddleware
import dev.alpas.auth.middleware.VerifiedEmailOnlyMiddleware
import dev.alpas.http.HttpCall
import dev.alpas.http.Method
import dev.alpas.routing.middleware.SignedRequestMiddleware
import uy.klutter.core.common.mustStartWith
import kotlin.reflect.KClass

class Route(
    val method: Method,
    var path: String,
    val middleware: MutableSet<KClass<out Middleware<HttpCall>>> = mutableSetOf(),
    val middlewareGroups: MutableSet<String> = mutableSetOf(),
    val handler: RouteHandler
) {
    constructor(method: Method, path: String, handler: RouteHandler) : this(
        method,
        path,
        mutableSetOf(),
        mutableSetOf(),
        handler
    )

    internal var name = ""
        private set

    internal var skipCsrfCheck = false
        private set

    var groupName = ""
        internal set

    var authChannel: String? = null
        internal set

    fun name(name: String): Route {
        this.name = name
        return this
    }

    internal fun authOnly(): Boolean {
        return middleware.contains(AuthOnlyMiddleware::class)
    }

    internal fun guestOnly(): Boolean {
        return middleware.contains(GuestOnlyMiddleware::class)
    }

    @ExperimentalStdlibApi
    fun methods(): List<Method> {
        return buildList(2) {
            if (method == Method.GET) {
                add(Method.HEAD)
            }
            add(method)
        }
    }

    internal fun build(loader: PackageClassLoader) {
        path = path.mustStartWith("/")
        name(combineNames(groupName, name))
        if (handler is DynamicControllerHandler) {
            handler.prepare(loader)
        }
    }

    fun middleware(
        middleware: KClass<out Middleware<HttpCall>>,
        vararg others: KClass<out Middleware<HttpCall>>
    ): Route {
        this.middleware.add(middleware)
        this.middleware.addAll(others)
        return this
    }

    fun mustBeAuthenticated(authChannel: String? = null): Route {
        middleware(AuthOnlyMiddleware::class as KClass<out Middleware<HttpCall>>)
        this.authChannel = authChannel
        return this
    }

    fun mustBeSigned(): Route {
        middleware(SignedRequestMiddleware::class as KClass<out Middleware<HttpCall>>)
        return this
    }

    fun mustBeVerified(): Route {
        middleware(VerifiedEmailOnlyMiddleware::class as KClass<out Middleware<HttpCall>>)
        return this
    }

    fun mustBeGuest(): Route {
        middleware(GuestOnlyMiddleware::class as KClass<out Middleware<HttpCall>>)
        return this
    }

    fun skipCsrfCheck(): Route {
        skipCsrfCheck = true
        return this
    }

    fun middleware(middleware: Set<KClass<out Middleware<HttpCall>>>): Route {
        this.middleware.addAll(middleware)
        return this
    }

    fun middlewareGroup(groupName: String): Route {
        middlewareGroups.add(groupName)
        return this
    }

    fun middlewareGroup(groupNames: List<String>): Route {
        middlewareGroups.addAll(groupNames)
        return this
    }

    internal fun handle(call: HttpCall) {
        handler.handle(call)
    }

    override fun toString() = "$name :: $method :: $path"
}
