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
        // When running on Windows, resource loader is not able to load a resource using
        // Windows path separator. As a workaround, we'll force using UNIX style path
        // unless SKIP_ENFORCE_UNIX_PATH environment variable is set to true.
        val separator = if (app.env("SKIP_ENFORCE_UNIX_PATH", false)) File.separator else "/"

        // In dev mode the mix manifest file could change when the developer is actively making changes to the
        // static assets such as .css, .vue, .js files etc. The manifest file shouldn't change in the prod
        // mode. To accommodate this, we'll make mix a singleton for prod and non-singleton in de mode.
        if (app.env.isDev || !::manifestMap.isInitialized) {
            val config = app.config<ViewConfig>()
            val mixManifestDirectory = config.mixManifestDirectory.mustStartWith(separator)
            val webDirectory = config.webDirectory
            val manifestPath = Paths.get(webDirectory, mixManifestDirectory, manifestFilename)
                .toString()
                .replace(File.separator, separator)
                .mustStartWith(separator)

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

