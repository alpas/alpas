package dev.alpas.view

import com.mitchellbosecke.pebble.extension.AbstractExtension

interface ViewRenderer {
    fun addConfigs(key: String, value: Map<String, Any?>)
    fun boot()
    fun extend(extension: AbstractExtension, vararg extensions: AbstractExtension)
    fun render(view: View?, extraContext: Map<String, Any?>): String
}
