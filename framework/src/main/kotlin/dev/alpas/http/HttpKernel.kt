package dev.alpas.http

import dev.alpas.Application
import dev.alpas.Kernel
import dev.alpas.Middleware
import dev.alpas.ServiceProvider
import dev.alpas.cookie.EncryptCookies
import dev.alpas.encryption.EncryptionServiceProvider
import dev.alpas.hashing.HashServiceProvider
import dev.alpas.http.middleware.SessionStart
import dev.alpas.http.middleware.VerifyCsrfToken
import dev.alpas.logging.LoggerServiceProvider
import dev.alpas.ozone.OzoneServiceProvider
import dev.alpas.queue.QueueServiceProvider
import dev.alpas.routing.RouteServiceProvider
import dev.alpas.session.SessionServiceProvider
import dev.alpas.view.ViewServiceProvider
import kotlin.reflect.KClass

open class HttpKernel : AlpasServer(), Kernel {
    private val routeEntryMiddlewareGroups: Map<String, List<KClass<out Middleware<HttpCall>>>> by lazy {
        val groups: HashMap<String, List<KClass<out Middleware<HttpCall>>>> = hashMapOf()
        groups["web"] = webMiddlewareGroup()
        registerRouteMiddlewareGroups(groups)
        groups
    }

    override fun boot(app: Application) {
        super<AlpasServer>.boot(app)
    }

    override fun routeEntryMiddlewareGroups(app: Application): Map<String, List<KClass<out Middleware<HttpCall>>>> {
        return routeEntryMiddlewareGroups
    }

    protected open fun registerRouteMiddlewareGroups(groups: HashMap<String, List<KClass<out Middleware<HttpCall>>>>) {
    }

    override fun serverEntryMiddleware(app: Application): Iterable<KClass<out Middleware<HttpCall>>> {
        val middleware = mutableListOf<KClass<out Middleware<HttpCall>>>()
        registerServerEntryMiddleware(middleware)
        return middleware
    }

    protected open fun registerServerEntryMiddleware(middleware: MutableList<KClass<out Middleware<HttpCall>>>) {
    }

    open fun webMiddlewareGroup(): List<KClass<out Middleware<HttpCall>>> {
        return listOf(SessionStart::class, VerifyCsrfToken::class, EncryptCookies::class)
    }

    override fun stop(app: Application) {
        app.logger.debug { "Stopping kernel $this" }
        super<AlpasServer>.stop(app)
    }

    override fun serviceProviders(app: Application): Iterable<KClass<out ServiceProvider>> {
        return listOf(
            LoggerServiceProvider::class,
            EncryptionServiceProvider::class,
            HashServiceProvider::class,
            SessionServiceProvider::class,
            RouteServiceProvider::class,
            ViewServiceProvider::class,
            OzoneServiceProvider::class,
            QueueServiceProvider::class
        )
    }
}

