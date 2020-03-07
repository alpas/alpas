package dev.alpas.http

import dev.alpas.Application
import dev.alpas.Middleware
import dev.alpas.http.jetty.AlpasServlet
import dev.alpas.http.jetty.EngineBase
import dev.alpas.http.jetty.HttpEngine
import org.eclipse.jetty.server.Server
import kotlin.reflect.KClass

typealias RouteEntryMiddlewareGroups = Map<String, List<KClass<out Middleware<HttpCall>>>>
typealias ServerEntryMiddleware = Iterable<KClass<out Middleware<HttpCall>>>

open class HttpServer {
    protected open val engine: EngineBase by lazy(LazyThreadSafetyMode.NONE) { HttpEngine() }
    private lateinit var server: Server

    open fun boot(
        app: Application,
        routeEntryMiddlewareGroups: RouteEntryMiddlewareGroups = emptyMap(),
        serverEntryMiddleware: ServerEntryMiddleware = emptyList()
    ) {
        disableJettyLogging()
        app.takeOff()
        val servlet = AlpasServlet(app, routeEntryMiddlewareGroups, serverEntryMiddleware)
        server = engine.start(app, servlet) ?: throw Exception("Error starting the server")
        app.logger.info { "${app.env("APP_NAME")} is available at ${server.uri}" }
    }

    open fun stop(app: Application) {
        app.logger.debug { "Stopping server $this" }
        server.stop()
    }

    open fun disableJettyLogging() {
        NoLogging.applyToJetty()
    }
}
