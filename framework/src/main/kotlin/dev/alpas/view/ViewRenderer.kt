package dev.alpas.view

import com.mitchellbosecke.pebble.extension.AbstractExtension
import dev.alpas.http.RenderContext
import dev.alpas.validation.SharedDataBag

interface ViewRenderer {
    fun extend(extension: AbstractExtension, vararg extensions: AbstractExtension)
    fun render(viewName: String?, sharedDataBag: SharedDataBag, viewArgs: Map<String, Any?>? = null): String {
        TODO()
    }

    fun render(template: String, args: Map<String, Any?>? = null): String {
        TODO()
    }

    fun render(context: RenderContext, viewArgs: Map<String, Any?>? = null): String {
        return render(context.viewName, context.sharedDataBag, viewArgs)
    }
}
