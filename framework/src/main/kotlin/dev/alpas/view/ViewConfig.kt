package dev.alpas.view

import dev.alpas.*
import uy.klutter.core.common.mustStartWith
import java.io.File
import java.nio.file.Paths

open class ViewConfig(env: Environment) : Config {
    open val isEnabled = true
    open val mixManifestDirectory = env("MIX_MANIFEST_DIRECTORY", "")
    open val templatesDirectory = "templates"
    open val templateExtension = "peb"
    open val mixManifestFilename = "mix-manifest.json"
    open var cacheTemplates = false
        protected set
    private var storageWebDirectory: String? = null

    override fun register(app: Application) {
        cacheTemplates = app.env.isProduction
        app.config<AppConfig>().also {
            if (it.enableStorageWebDirectory) {
                storageWebDirectory = it.storageWebDirectory
            }
        }
    }

    open fun configsAvailableForView(): Map<String, String?> = emptyMap()

    open val mixManifestPath: String by lazy {
        if (storageWebDirectory != null) {
            env.storagePath(*RESOURCES_DIRS, "web", mixManifestFilename)
        } else {
            // Even on Windows, resource loader requires the path to be UNIX style path
            val separator = "/"
            Paths.get("web", mixManifestDirectory, mixManifestFilename)
                .toString()
                .replace(File.separator, separator)
                .mustStartWith(separator)
        }
    }
}
