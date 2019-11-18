package dev.alpas

import java.net.URL

class ResourceLoader(entryClass: Class<*>) {
    private val entryClassLoader by lazy { entryClass.classLoader }
    fun load(resource: String): URL? {
        return entryClassLoader.getResource(resource)
    }
}
