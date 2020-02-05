package dev.alpas.view

import com.mitchellbosecke.pebble.PebbleEngine
import com.mitchellbosecke.pebble.extension.AbstractExtension
import com.mitchellbosecke.pebble.loader.ClasspathLoader
import com.mitchellbosecke.pebble.loader.FileLoader
import com.mitchellbosecke.pebble.loader.Loader
import dev.alpas.Application
import dev.alpas.RESOURCES_DIRS
import dev.alpas.make
import dev.alpas.validation.SharedDataBag
import uy.klutter.core.common.mustEndWith
import uy.klutter.core.common.mustStartWith
import java.io.File
import java.io.StringWriter

open class PebbleViewRenderer(private val app: Application) : ViewRenderer {
    private var templateExtension: String = ""
    private val engine: PebbleEngine by lazy { engineBuilder.cacheActive(app.env.isProduction).build() }

    private val engineBuilder by lazy {
        val viewConfig = app.make<ViewConfig>()
        PebbleEngine.Builder()
            .loader(loader(app, viewConfig))
            .apply { if (app.env.isDev) templateCache(null) }
    }

    protected open fun loader(app: Application, config: ViewConfig): Loader<String> {
        // Check if storage/src/main/resources/templates directory exists if so use the file Loader instead.
        templateExtension = config.templateExtension.mustStartWith(".")
        val templatesDir = "templates"
        val storageTemplatesPath = app.env.storagePath(*RESOURCES_DIRS, templatesDir)
        if (File(storageTemplatesPath).exists()) {
            return FileLoader().apply { prefix = storageTemplatesPath }
        }
        return ClasspathLoader().apply { prefix = templatesDir }
    }

    override fun extend(extension: AbstractExtension, vararg extensions: AbstractExtension) {
        engineBuilder.extension(extension)
        extensions.forEach {
            engineBuilder.extension(it)
        }
    }

    override fun render(viewName: String?, sharedDataBag: SharedDataBag, viewArgs: Map<String, Any?>?): String {
        val view = viewName ?: return ""

        return StringWriter().use { writer ->
            val template = engine.getTemplate(view.mustEndWith(templateExtension))
            val templateContext = (viewArgs ?: emptyMap()) + (sharedDataBag.all())
            template.evaluate(writer, templateContext)
            writer.toString()
        }
    }

    override fun render(template: String, args: Map<String, Any?>?): String {
        return StringWriter().use { writer ->
            engine.getLiteralTemplate(template).evaluate(writer, args ?: emptyMap())
            writer.toString()
        }
    }
}
