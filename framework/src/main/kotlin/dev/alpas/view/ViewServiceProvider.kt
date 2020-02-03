package dev.alpas.view

import com.mitchellbosecke.pebble.template.EvaluationContext
import dev.alpas.*
import dev.alpas.http.HttpCall
import dev.alpas.view.extensions.PebbleExtension
import dev.alpas.view.extensions.PebbleExtensionWrapper
import dev.alpas.view.extensions.PebbleExtensions

internal val EvaluationContext.httpCall: HttpCall
    get() {
        return getVariable("call") as HttpCall
    }

data class TagContext(val call: HttpCall, val lineNumber: Int, val templateName: String, val context: EvaluationContext)

@Suppress("unused")
open class ViewServiceProvider : ServiceProvider {
    private lateinit var conditionalTags: ConditionalTags
    private lateinit var customTags: CustomTags

    override fun register(app: Application, loader: PackageClassLoader) {
        val viewConfig = app.config { ViewConfig(app.env) }
        if (!viewConfig.isEnabled) {
            return
        }
        viewConfig.register(app)

        val viewRenderer = viewRenderer(app)
        conditionalTags = ConditionalTagsImpl(viewRenderer)
        customTags = CustomTagsImpl(viewRenderer)

        registerPebbleExtensions(app, loader, conditionalTags, customTags)

        app.apply {
            bind(ConditionalTags::class.java, conditionalTags)
            bind(CustomTags::class.java, customTags)
            singleton(Mix(this))
            singleton(viewRenderer)
        }
    }

    private fun registerPebbleExtensions(
        app: Application,
        loader: PackageClassLoader,
        conditionalTags: ConditionalTags,
        customTags: CustomTags
    ) {
        loader.classesImplementing(PebbleExtension::class) {
            app.logger.debug { "Discovered Pebble Extension: ${it.name}" }
            app.singleton(it.loadClass())
        }
        app.bind(PebbleExtensions::class.java)

        app.makeMany<PebbleExtension>().forEach {
            app.logger.debug { "Registering Pebble Extension: ${it.javaClass.name}" }
            it.register(app, conditionalTags, customTags)
        }
    }

    open fun viewRenderer(app: Application): ViewRenderer = PebbleViewRenderer(app)

    @Suppress("UNCHECKED_CAST")
    override fun boot(app: Application) {
        if (!app.config<ViewConfig>().isEnabled) {
            return
        }

        val renderer = app.make<ViewRenderer>()
        app.makeMany<PebbleExtension>().forEach {
            app.logger.debug { "Booting and extending Pebble with: ${it.javaClass.name}" }
            it.boot(app)
            renderer.extend(PebbleExtensionWrapper(it, app))
        }
    }
}
