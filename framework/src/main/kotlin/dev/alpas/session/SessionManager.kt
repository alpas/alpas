package dev.alpas.session

import dev.alpas.Application
import dev.alpas.config
import org.eclipse.jetty.server.session.DefaultSessionCache
import org.eclipse.jetty.server.session.FileSessionDataStore
import org.eclipse.jetty.server.session.NullSessionCache
import org.eclipse.jetty.server.session.NullSessionDataStore
import org.eclipse.jetty.server.session.SessionCache
import org.eclipse.jetty.server.session.SessionDataStore
import org.eclipse.jetty.server.session.SessionHandler
import java.io.File

class SessionManager(private val app: Application) {
    private var sessionConfig = app.config { SessionConfig(app.env) }

    fun handler(): SessionHandler {
        // todo: do we need to set the cookie if the request is a json request?
        return SessionHandler().apply {
            httpOnly = sessionConfig.httpOnly
            sessionCookieConfig.apply {
                name = sessionConfig.cookieName
                maxAge = sessionConfig.lifetime.seconds.toInt()
                path = sessionConfig.path
                domain = sessionConfig.domain
                isSecure = sessionConfig.secure
            }
            sessionCache = createCacheDriver(this)
        }
    }

    private fun createFileDriver(): SessionDataStore {
        val baseDir = sessionConfig.storePath
        val fileSessionDataStore = FileSessionDataStore()
        fileSessionDataStore.storeDir = File(baseDir).apply { mkdir() }
        return fileSessionDataStore
    }

    private fun createCacheDriver(sessionHandler: SessionHandler): SessionCache {
        // actual store
        val store = when (sessionConfig.storeDriver) {
            SessionStoreDriver.FILE -> {
                app.logger.debug { "Session manager is set to file based. The sessions will be saved in: ${sessionConfig.storePath} directory." }
                createFileDriver()
            }
            SessionStoreDriver.SKIP -> {
                app.logger.debug { "Session driver is set to null. Sessions won't persist." }
                createNullDriver()
            }
        }

        // caching store
        return (when (sessionConfig.cacheDriver) {
            SessionCacheDriver.MEMORY -> DefaultSessionCache(sessionHandler)
            SessionCacheDriver.SKIP -> NullSessionCache(sessionHandler)
        }).apply { sessionDataStore = store }
    }

    private fun createNullDriver(): SessionDataStore {
        return NullSessionDataStore()
    }
}
