package dev.alpas.http.jetty

import dev.alpas.AppConfig
import dev.alpas.Container
import dev.alpas.Environment
import dev.alpas.make
import org.eclipse.jetty.server.*

class HttpEngine : EngineBase() {
    override fun connector(server: Server, container: Container): ServerConnector {
        val config = container.make<AppConfig>()
        val httpConfig = HttpConfiguration().apply {
            sendServerVersion = false
            addCustomizer(ForwardedRequestCustomizer())
        }
        val env = container.make<Environment>()
        return ServerConnector(server, HttpConnectionFactory(httpConfig)).also { connector ->
            val (host, port) = hostAndPort(config, env.isDev)
            connector.host = host
            connector.port = port
            connector.idleTimeout = config.connectionTimeOut.toMillis()
        }
    }
}
