package dev.alpas.http

import dev.alpas.Application
import dev.alpas.ResourceLoader
import dev.alpas.appConfig
import dev.alpas.isOneOf
import dev.alpas.make
import org.eclipse.jetty.server.handler.ResourceHandler
import org.eclipse.jetty.server.handler.gzip.GzipHandler
import org.eclipse.jetty.util.resource.Resource

class StaticAssetHandler(app: Application) {
    private val gzipHandlers: List<GzipHandler> by lazy { makeHandlers(app) }

    private fun makeHandlers(app: Application): List<GzipHandler> {
        val staticDirs = app.appConfig().staticDirs
        if (staticDirs.isEmpty()) {
            return emptyList()
        }
        val resourceLoader = app.make<ResourceLoader>()
        val handlers = mutableListOf<GzipHandler>()
        staticDirs.forEach { staticDir ->
            if (resourceLoader.load(staticDir) == null) {
                app.logger.warn { "Static directory $staticDir doesn't exist." }
            } else {
                GzipHandler().apply {
                    handler = ResourceHandler().apply {
                        resourceBase = Resource.newClassPathResource(staticDir).toString()
                        isDirAllowed = false
                        isEtags = true
                    }
                    start()
                    handlers.add(this)
                }
            }
        }
        return handlers
    }

    fun handle(call: HttpCall): Boolean {
        if (call.method.isOneOf(Method.GET, Method.HEAD)) {
            gzipHandlers.forEach { handler ->
                val resource = (handler.handler as ResourceHandler).getResource(call.uri)
                if (resource != null && resource.exists() && !resource.isDirectory) {
                    handler.handle(call.uri, call.jettyRequest, call.jettyRequest, call.servletResponse)
                    call.logger.debug { "Http call for ${call.url} is handled by $this" }
                    return true
                }
            }
        }
        return false
    }
}
