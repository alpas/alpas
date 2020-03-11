package dev.alpas.view

import dev.alpas.*
import java.io.File
import java.nio.file.Paths

open class ViewConfig(env: Environment) : Config {
    open val isEnabled get() = true
    open val mixManifestDirectory = env("MIX_MANIFEST_DIRECTORY", "")
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
        // If storageWebDirectory is set and that a mix file exists in that location, let's load that file
        val symlinkedMixFile = env.storagePath(*RESOURCES_DIRS, "web", mixManifestFilename)
        if (storageWebDirectory != null && File(symlinkedMixFile).exists()) {
            symlinkedMixFile
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
