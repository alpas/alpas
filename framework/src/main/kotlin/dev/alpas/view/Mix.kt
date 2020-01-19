package dev.alpas.view

import dev.alpas.*
import uy.klutter.core.common.mustStartWith
import java.io.File
import java.nio.file.Paths

internal class Mix(private val app: Application) {
    private lateinit var manifestMap: Map<String, String>
    private val manifestFilename = "mix-manifest.json"

    @Synchronized
    operator fun invoke(path: String, manifestDirectory: String = ""): String {
        // In dev mode the mix manifest file could change when the developer is actively making changes to the
        // static assets such as .css, .vue, .js files etc. The manifest file shouldn't change in the prod
        // mode. To accommodate this, we'll make mix a singleton for prod and non-singleton in de mode.
        val separator = File.separator
        if (app.env.isDev || !::manifestMap.isInitialized) {
            val config = app.config<ViewConfig>()
            val mixManifestDirectory = config.mixManifestDirectory.mustStartWith(separator)
            val webDirectory = config.webDirectory
            val manifestPath = Paths.get(webDirectory, mixManifestDirectory, manifestFilename).toString().let {
                // When running from GitBash, resource loader is not able to load a resource using
                // Windows path separator. As a workaround, we'll force using UNIX style path
                // if alpas_enforce_unix_path environment variable is set to true.
                val enforceUnixPath = System.getenv("alpas_enforce_unix_path")
                if (enforceUnixPath != null) {
                    app.logger.debug { "Modifying manifest path separator because enforce UNIX path is set." }
                    it.replace(separator, "\\")
                } else {
                    it
                }
            }
            app.logger.debug { "Mix manifest file will be loaded from $manifestPath" }
            val text = app.make<ResourceLoader>().load(manifestPath)?.readText()
                ?: throw Exception("Mix manifestMap file $manifestPath doesn't exist")
            manifestMap = JsonSerializer.deserialize<Map<String, String>>(text)
        }
        val manifest = manifestMap[path.mustStartWith(separator)] ?: throw Exception("Unable to locate Mix file: $path")
        app.logger.debug { "Loading mix file $manifest" }
        return if (manifestDirectory.isEmpty()) {
            manifest
        } else {
            "${manifestDirectory.mustStartWith(separator)}$manifest"
        }
    }
}

