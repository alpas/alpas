package dev.alpas

import java.net.URL

class ResourceLoader(private val entryClass: Class<*>) {
    fun load(resource: String): URL? {
        return entryClass.getResource(resource)
    }
}
