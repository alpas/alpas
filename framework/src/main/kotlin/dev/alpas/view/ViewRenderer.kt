package dev.alpas.view

import com.mitchellbosecke.pebble.extension.AbstractExtension
import dev.alpas.http.RenderContext
import dev.alpas.validation.SharedDataBag

interface ViewRenderer {
    fun extend(extension: AbstractExtension, vararg extensions: AbstractExtension)

    @Deprecated(
        "This method is deprecated. Use renderView() instead.",
        ReplaceWith("renderView(viewName, sharedDataBag, viewArgs)")
    )
    fun render(viewName: String?, sharedDataBag: SharedDataBag, viewArgs: Map<String, Any?>? = null): String {
        return renderView(viewName, sharedDataBag, viewArgs)
    }

    @Deprecated(
        "This method is deprecated. Use renderTemplate() instead.",
        ReplaceWith("renderTemplate(template, SharedDataBag(), args)", "dev.alpas.validation.SharedDataBag")
    )
    fun render(template: String, args: Map<String, Any?>? = null): String {
        return renderTemplate(template, SharedDataBag(), args)
    }

    fun renderView(viewName: String?, sharedDataBag: SharedDataBag, viewArgs: Map<String, Any?>? = null): String {
        TODO()
    }

    fun renderTemplate(template: String, sharedDataBag: SharedDataBag, args: Map<String, Any?>? = null): String {
        TODO()
    }

    fun renderContext(context: RenderContext, viewArgs: Map<String, Any?>? = null): String {
        return renderView(context.viewName, context.sharedDataBag, viewArgs)
    }
}
