package dev.alpas.view

import com.mitchellbosecke.pebble.extension.AbstractExtension
import dev.alpas.http.RenderContext
import dev.alpas.validation.SharedDataBag

interface ViewRenderer {
    fun extend(extension: AbstractExtension, vararg extensions: AbstractExtension)
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
