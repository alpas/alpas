package dev.alpas.http.jetty

import dev.alpas.AppConfig
import dev.alpas.Container
import dev.alpas.config
import dev.alpas.http.SessionListener
import dev.alpas.session.SessionManager
import dev.alpas.tryMake
import org.eclipse.jetty.server.ForwardedRequestCustomizer
import org.eclipse.jetty.server.HttpConfiguration
import org.eclipse.jetty.server.HttpConnectionFactory
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.server.handler.HandlerCollection
import org.eclipse.jetty.server.session.SessionHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.eclipse.jetty.util.thread.QueuedThreadPool

internal class JettyServer {
    internal fun start(app: Container, servlet: AlpasServlet): Server? {
        val config = app.config<AppConfig>()
        val sessionHandler = app.tryMake<SessionManager>()?.handler()
        return server(servlet, config, sessionHandler).also {
            it.start()
        }
    }

    private fun server(servlet: AlpasServlet, config: AppConfig, sessionHandler: SessionHandler?): Server {
        return Server(QueuedThreadPool(config.maxThreads, config.minThreads)).also { server ->
            val httpConfig = HttpConfiguration().apply {
                sendServerVersion = false
                addCustomizer(ForwardedRequestCustomizer())
            }
            val connector = ServerConnector(server, HttpConnectionFactory(httpConfig)).apply {
                if (!config.enableNetworkShare) {
                    host = "localhost"
                }
                port = config.appPort
                idleTimeout = config.connectionTimeOut.toMillis()
            }
            server.addConnector(connector)
            val requestHandler = JettyRequestHandler().also { reqHandler ->
                if (sessionHandler != null) {
                    reqHandler.sessionHandler = sessionHandler
                    reqHandler.sessionHandler.addEventListener(SessionListener())
                }
                reqHandler.addServlet(ServletHolder(servlet), "/")
            }
            server.setAttribute("is-default-server", true)
            server.handler = HandlerCollection(true, requestHandler)
        }
    }
}
