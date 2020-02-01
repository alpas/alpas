package dev.alpas.http

import dev.alpas.*
import org.eclipse.jetty.server.handler.ResourceHandler
import org.eclipse.jetty.server.handler.gzip.GzipHandler
import org.eclipse.jetty.util.resource.Resource
import java.io.File

class StaticAssetHandler(app: Application) {
    private val gzipHandlers: List<GzipHandler> by lazy { makeHandlers(app) }

    private fun makeHandlers(app: Application): List<GzipHandler> {
        val appConfig = app.appConfig()

        val staticDirs = appConfig.staticDirs
        if (staticDirs.isEmpty()) {
            app.logger.info { "Empty static directories. No static content will be served." }
            return emptyList()
        }

        val resourceLoader = app.make<ResourceLoader>()
        val handlers = mutableListOf<GzipHandler>()

        staticDirs.mapNotNull { staticDir ->
            if (resourceLoader.loadResource(staticDir) == null) {
                val message = "Static resource directory $staticDir doesn't exist."
                when {
                    File(staticDir).exists() -> {
                        staticDir
                    }
                    appConfig.throwOnMissingStaticDirectories -> {
                        throw RuntimeException(message)
                    }
                    else -> {
                        app.logger.warn { "Missing static assets directory $staticDir. Ignoring." }
                        null
                    }
                }
            } else {
                Resource.newClassPathResource(staticDir).toString()
            }
        }.forEach {
            app.logger.info { "Adding static assets directory $it." }
            GzipHandler().apply {
                handler = ResourceHandler().apply {
                    resourceBase = it
                    isDirAllowed = false
                    isEtags = true
                }
                start()
                handlers.add(this)
            }
        }
        return handlers
    }

    fun handle(call: HttpCall): Boolean {
        if (call.method.isOneOf(Method.GET, Method.HEAD)) {
            gzipHandlers.forEach { handler ->
                val resourceHandler = handler.handler as ResourceHandler
                val resource = resourceHandler.getResource(call.uri)
                if (resource != null && resource.exists() && !resource.isDirectory) {
                    handler.handle(call.uri, call.jettyRequest, call.jettyRequest, call.servletResponse)
                    call.logger.debug { "Static asset ${call.url} is served from ${resourceHandler.baseResource.name}." }
                    return true
                }
            }
        }
        return false
    }
}
