package dev.alpas.view

import dev.alpas.Application
import dev.alpas.Config
import dev.alpas.Environment

open class ViewConfig(env: Environment) : Config {
    open val isEnabled = true
    open val mixManifestDirectory = env("MIX_MANIFEST_DIRECTORY", "")
    open val templatesDirectory = "templates"
    open val templateExtension = "twig"
    open val webDirectory = "web"
    open var cacheTemplates = false
        protected set

    override fun register(app: Application) {
        cacheTemplates = app.env.isProduction
    }

    open fun configsAvailableForView(): Map<String, String?> = mapOf()
}
