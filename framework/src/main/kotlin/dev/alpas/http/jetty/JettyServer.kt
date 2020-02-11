package dev.alpas.http.jetty

import dev.alpas.AppConfig
import dev.alpas.Application
import dev.alpas.config
import dev.alpas.http.SessionListener
import dev.alpas.session.SessionManager
import dev.alpas.tryMake
import org.eclipse.jetty.server.*
import org.eclipse.jetty.server.handler.HandlerCollection
import org.eclipse.jetty.server.session.SessionHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.eclipse.jetty.util.thread.QueuedThreadPool
import java.net.BindException
import java.net.InetSocketAddress
import java.nio.channels.ServerSocketChannel

internal class JettyServer {
    internal fun start(app: Application, servlet: AlpasServlet): Server? {
        val config = app.config<AppConfig>()
        val host = if (config.enableNetworkShare) null else "localhost"
        var port = config.appPort

        if (app.env.isDev && !isTcpPortAvailable(port, host)) {
            port = getFreePort(port)
            app.logger.warn { "App port ${config.appPort} is not available. We'll use the port '$port' instead." }
        }

        val sessionHandler = app.tryMake<SessionManager>()?.handler()
        return server(servlet, config, sessionHandler, host, port).also { it.start() }
    }

    private fun server(
        servlet: AlpasServlet,
        config: AppConfig,
        sessionHandler: SessionHandler?,
        serverHost: String?,
        serverPort: Int
    ): Server {
        return Server(QueuedThreadPool(config.maxThreads, config.minThreads)).also { server ->
            val httpConfig = HttpConfiguration().apply {
                sendServerVersion = false
                addCustomizer(ForwardedRequestCustomizer())
            }
            val connector = ServerConnector(server, HttpConnectionFactory(httpConfig)).apply {
                host = serverHost
                port = serverPort
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

    private fun getFreePort(startPort: Int): Int {
        var freePort = startPort
        if (!isTcpPortAvailable(freePort)) {
            do {
                freePort += 10
            } while (!isTcpPortAvailable(freePort))
        }
        return freePort
    }

    private fun isTcpPortAvailable(port: Int, host: String? = "localhost"): Boolean {
        return ServerSocketChannel.open().use {
            try {
                val bindAddress = InetSocketAddress(host, port)
                it.socket().reuseAddress = true
                it.socket().bind(bindAddress)
                true
            } catch (ex: BindException) {
                false
            }
        }
    }
}
