package dev.alpas.view

import dev.alpas.*
import uy.klutter.core.common.mustStartWith

class Mix(private val app: Application) {
    private val cachedManifestMap by lazy { loadManifestMap() }
    private val mixManifestPath = app.config<ViewConfig>().mixManifestPath
    private val enableStorageWebDirectory = app.config<AppConfig>().enableStorageWebDirectory

    val version: String
        get() {
            return if (app.env.isDev) {
                calculateVersion()
            } else {
                cachedVersion
            }
        }

    private val manifestMap: Map<String, String>
        get() {
            // In dev mode the mix manifest file could change when the developer is actively making
            // changes to the static assets such as .css, .vue, .js files, etc. Also, in dev mode,
            // if the user has not linked a web directory then it doesn't matter anyway.
            return if (app.env.isDev && enableStorageWebDirectory) {
                loadManifestMap()
            } else {
                cachedManifestMap
            }
        }

    private val cachedVersion by lazy { calculateVersion() }
    private val resourceLoader by lazy { app.make<ResourceLoader>() }

    private fun calculateVersion(): String {
        return resourceLoader.calculateVersion(mixManifestPath)
    }

    internal operator fun invoke(path: String, manifestDirectory: String = ""): String {
        // Even on Windows, resource loader requires the path to be UNIX style path
        val separator = "/"

        val manifest = manifestMap[path.mustStartWith(separator)] ?: throw Exception("Unable to locate Mix file: $path")
        app.logger.debug { "Loading mix file $manifest" }

        return if (manifestDirectory.isEmpty()) {
            manifest
        } else {
            "${manifestDirectory.mustStartWith(separator)}$manifest"
        }
    }

    private fun loadManifestMap(): Map<String, String> {
        val text = resourceLoader.load(mixManifestPath)?.readText()
            ?: throw Exception("Mix manifestMap file $mixManifestPath doesn't exist")
        return JsonSerializer.deserialize<HashMap<String, String>>(text)
    }
}
