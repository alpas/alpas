package dev.alpas.view

import com.mitchellbosecke.pebble.PebbleEngine
import com.mitchellbosecke.pebble.extension.AbstractExtension
import com.mitchellbosecke.pebble.loader.ClasspathLoader
import com.mitchellbosecke.pebble.loader.FileLoader
import com.mitchellbosecke.pebble.loader.Loader
import dev.alpas.Application
import dev.alpas.RESOURCES_DIRS
import dev.alpas.http.RenderContext
import dev.alpas.make
import uy.klutter.core.common.mustStartWith
import java.io.File
import java.io.StringWriter

open class PebbleViewRenderer(private val app: Application) : ViewRenderer {
    private lateinit var templateExtension: String
    private val engine: PebbleEngine by lazy { engineBuilder.cacheActive(app.env.isProduction).build() }

    private val engineBuilder by lazy {
        val viewConfig = app.make<ViewConfig>()
        templateExtension = viewConfig.templateExtension.mustStartWith(".")
        PebbleEngine.Builder()
            .loader(loader(app, viewConfig))
            .apply { if (app.env.isDev) templateCache(null) }
    }

    protected open fun loader(app: Application, config: ViewConfig): Loader<String> {
        // Check if storage/src/main/resources/templates directory exists if so use the file Loader instead.
        val storageTemplatesPath = app.env.storagePath(*RESOURCES_DIRS, "templates")
        if (File(storageTemplatesPath).exists()) {
            return FileLoader().apply { prefix = storageTemplatesPath }
        }
        return ClasspathLoader().apply { prefix = config.templatesDirectory }
    }

    override fun extend(extension: AbstractExtension, vararg extensions: AbstractExtension) {
        (listOf(extension) + extensions).forEach {
            engineBuilder.extension(it)
        }
    }

    override fun render(context: RenderContext, viewArgs: Map<String, Any?>?): String {
        val view = context.viewName ?: return ""

        return StringWriter().let { writer ->
            val template = engine.getTemplate("${view}$templateExtension")

            try {
                val templateContext = (viewArgs ?: emptyMap()) + (context.sharedDataBag.all())
                template.evaluate(writer, templateContext)
                writer.toString()
            } catch (e: Exception) {
                throw e
            }
        }
    }

    override fun render(template: String, args: Map<String, Any?>?): String {
        return StringWriter().let { writer ->
            try {
                engine.getLiteralTemplate(template).evaluate(writer, args ?: emptyMap())
                writer.toString()
            } catch (e: Exception) {
                throw e
            }
        }
    }
}
