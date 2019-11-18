package dev.alpas.view

import com.mitchellbosecke.pebble.PebbleEngine
import com.mitchellbosecke.pebble.loader.ClasspathLoader
import dev.alpas.Application
import dev.alpas.config
import uy.klutter.core.common.mustStartWith
import java.io.StringWriter

internal class PebbleViewRenderer(private val app: Application) : ViewRenderer {
    private lateinit var templateExtension: String
    private var viewConfigs = mutableMapOf<String, Any?>()
    private val engine: PebbleEngine by lazy { engineBuilder.cacheActive(app.env.isProduction).build() }

    private val engineBuilder by lazy {
        val viewConfig = app.config<ViewConfig>()
        templateExtension = viewConfig.templateExtension.mustStartWith(".")
        val engineBuilder = PebbleEngine.Builder()
            .loader(ClasspathLoader().apply {
                prefix = viewConfig.templatesDirectory
            })
        if (app.env.isDev) {
            engineBuilder.templateCache(null)
        }
        engineBuilder
    }

    override fun boot() {
    }

    override fun extend(extension: ViewExtension, vararg extensions: ViewExtension) {
        engineBuilder.extension(extension)
        engineBuilder.extension(*extensions)
    }

    override fun addConfigs(key: String, value: Map<String, Any?>) {
        viewConfigs[key] = value
    }

    override fun render(view: View?, extraContext: Map<String, Any?>): String {
        if (view == null) return ""
        return StringWriter().let { writer ->
            val template = engine.getTemplate("${view.name}$templateExtension")

            try {
                val context = (view.args?.toMutableMap() ?: mutableMapOf()) + extraContext + viewConfigs
                template.evaluate(writer, context)
                writer.toString()
            } catch (e: Exception) {
                throw e
            }
        }
    }
}
