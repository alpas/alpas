package dev.alpas.view

import dev.alpas.*
import uy.klutter.core.common.mustStartWith
import java.nio.file.Paths

internal class Mix(private val app: Application) {
    private lateinit var manifestMap: Map<String, String>

    @Synchronized
    operator fun invoke(path: String, manifestDirectory: String = ""): String {
        // In dev mode the mix manifest file could change when the developer is actively making changes to the
        // static assets such as .css, .vue, .js files etc. The manifest file shouldn't change in the prod
        // mode. To accommodate this, we'll make mix a singleton for prod and non-singleton in de mode.
        if (app.env.isDev || !::manifestMap.isInitialized) {
            val config = app.config<ViewConfig>()
            val mixManifestDirectory = config.mixManifestDirectory.mustStartWith("/")
            val webDirectory = config.webDirectory
            val manifestPath = Paths.get(webDirectory, mixManifestDirectory, "mix-manifest.json").toString()
            val text = app.make<ResourceLoader>().load(manifestPath)?.readText()
                ?: throw Exception("Mix manifestMap file $manifestPath doesn't exist")
            manifestMap = JsonSerializer.deserialize<Map<String, String>>(text)
        }
        val manifest = manifestMap[path.mustStartWith("/")] ?: throw Exception("Unable to locate Mix file: $path")
        return if (manifestDirectory.isEmpty()) {
            manifest
        } else {
            "${manifestDirectory.mustStartWith("/")}$manifest"
        }
    }
}

