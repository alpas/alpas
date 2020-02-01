package dev.alpas.view

import com.mitchellbosecke.pebble.extension.AbstractExtension
import dev.alpas.http.RenderContext

interface ViewRenderer {
    fun extend(extension: AbstractExtension, vararg extensions: AbstractExtension)
    fun render(context: RenderContext, viewArgs: Map<String, Any?>? = null): String
    fun render(template: String, args: Map<String, Any?>? = null): String
}
