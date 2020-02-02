package dev.alpas.view

import com.mitchellbosecke.pebble.template.EvaluationContext
import dev.alpas.Application
import dev.alpas.ServiceProvider
import dev.alpas.config
import dev.alpas.http.HttpCall
import dev.alpas.make
import dev.alpas.view.extensions.BuiltInExtensions

internal val EvaluationContext.httpCall: HttpCall
    get() {
        return getVariable("call") as HttpCall
    }

data class TagContext(val call: HttpCall, val lineNumber: Int, val templateName: String, val context: EvaluationContext)

@Suppress("unused")
open class ViewServiceProvider : ServiceProvider {
    private lateinit var conditionalTags: ConditionalTags
    private lateinit var customTags: CustomTags

    override fun register(app: Application) {
        val viewConfig = app.config { ViewConfig(app.env) }
        if (!viewConfig.isEnabled) {
            return
        }
        viewConfig.register(app)

        // todo: add all extensions from user land
        val viewRenderer = viewRenderer(app)
        conditionalTags = ConditionalTagsImpl(viewRenderer)
        customTags = CustomTagsImpl(viewRenderer)

        app.apply {
            bind(ConditionalTags::class.java, conditionalTags)
            bind(CustomTags::class.java, customTags)
            singleton(Mix(this))
            singleton(viewRenderer)
        }
    }

    open fun viewRenderer(app: Application): ViewRenderer = PebbleViewRenderer(app)

    @Suppress("UNCHECKED_CAST")
    override fun boot(app: Application) {
        if (!app.config<ViewConfig>().isEnabled) {
            return
        }

        app.make<ViewRenderer>().apply {
            extend(BuiltInExtensions(app))
        }
    }
}
