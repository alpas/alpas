package dev.alpas.http

import dev.alpas.Application
import dev.alpas.Kernel
import dev.alpas.Middleware
import dev.alpas.cookie.EncryptCookies
import dev.alpas.http.middleware.SessionStart
import dev.alpas.http.middleware.VerifyCsrfToken
import kotlin.reflect.KClass

open class HttpKernel : AlpasServer(), Kernel {
    open val middlewareGroups: Map<String, List<KClass<out Middleware<HttpCall>>>> =
        mapOf("web" to listOf(SessionStart::class, VerifyCsrfToken::class, EncryptCookies::class))

    override fun boot(app: Application) {
        super<AlpasServer>.boot(app)
    }

    override fun routeEntryMiddlewareGroups(app: Application): Map<String, List<KClass<out Middleware<HttpCall>>>> {
        return middlewareGroups
    }

    override fun stop(app: Application) {
        app.logger.debug { "Stopping kernel $this" }
        super<AlpasServer>.stop(app)
    }
}
