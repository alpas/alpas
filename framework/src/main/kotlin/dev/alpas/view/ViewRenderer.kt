package dev.alpas.view

interface ViewRenderer {
    fun addConfigs(key: String, value: Map<String, Any?>)
    fun boot()
    fun extend(extension: ViewExtension, vararg extensions: ViewExtension)
    fun render(view: View?, extraContext: Map<String, Any?>): String
}
