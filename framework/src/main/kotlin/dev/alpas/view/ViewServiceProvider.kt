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
data class FunctionContext(
    val call: HttpCall,
    val args: Map<String, Any>?,
    val context: EvaluationContext,
    val lineNumber: Int,
    val templateName: String
)

@Suppress("unused")
open class ViewServiceProvider : ServiceProvider {
    private lateinit var conditionalTags: ConditionalTags
    private lateinit var customTags: CustomTags
    private lateinit var customFunctions: CustomFunctions

    override fun register(app: Application, loader: PackageClassLoader) {
        val viewConfig = app.config { ViewConfig(app.env) }
        if (!viewConfig.isEnabled) {
            return
        }
        viewConfig.register(app)

        val viewRenderer = viewRenderer(app)
        conditionalTags = ConditionalTagsImpl(viewRenderer)
        customTags = CustomTagsImpl(viewRenderer)
        customFunctions = CustomFunctionsImpl(viewRenderer)

        registerPebbleExtensions(app, loader, conditionalTags, customTags, customFunctions)

        app.apply {
            singleton(conditionalTags)
            singleton(customTags)
            singleton(customFunctions)
            singleton(Mix(this))
            singleton(viewRenderer)
        }
    }

    private fun registerPebbleExtensions(
        app: Application,
        loader: PackageClassLoader,
        conditionalTags: ConditionalTags,
        customTags: CustomTags,
        customFunctions: CustomFunctions
    ) {
        loader.classesImplementing(PebbleExtension::class) {
            app.logger.debug { "Discovered Pebble Extension: ${it.name}" }
            app.singleton(it.loadClass())
        }
        app.bind(PebbleExtensions::class.java)

        app.makeMany<PebbleExtension>().forEach {
            app.logger.debug { "Registering Pebble Extension: ${it.javaClass.name}" }
            it.register(app, conditionalTags)
            it.register(app, customTags)
            it.register(app, customFunctions)
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
