package dev.alpas.http

import dev.alpas.Application
import dev.alpas.Middleware
import dev.alpas.http.jetty.AlpasServlet
import dev.alpas.http.jetty.JettyServer
import org.eclipse.jetty.server.Server
import kotlin.reflect.KClass

abstract class AlpasServer {
    lateinit var server: Server

    protected open fun boot(app: Application) {
        disableJettyLogging()
        app.takeOff()
        val servlet = AlpasServlet(app, routeEntryMiddlewareGroups(app), serverEntryMiddleware(app))
        server = JettyServer().start(app, servlet) ?: throw Exception("Error starting the server")
        app.logger.info { "${app.env("APP_NAME")} is available at ${server.uri}" }
    }

    // All the middleware groups that are available for applying to each request
    // once the target route has matched but before actually hitting the route.
    protected open fun routeEntryMiddlewareGroups(app: Application): Map<String, List<KClass<out Middleware<HttpCall>>>> =
        emptyMap()

    // These middleware are applied to each requests as received by the server
    // regardless of whether the target route has matched or not.
    protected open fun serverEntryMiddleware(app: Application): Iterable<KClass<out Middleware<HttpCall>>> = emptyList()

    open fun stop(app: Application) {
        app.logger.debug { "Stopping server $this" }
        server.stop()
    }

    protected open fun disableJettyLogging() {
        NoLogging.applyToJetty()
    }
}
