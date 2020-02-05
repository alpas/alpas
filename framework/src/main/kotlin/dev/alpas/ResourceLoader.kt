@file:Suppress("EXPERIMENTAL_API_USAGE")

package dev.alpas

import java.io.File
import java.net.URL
import java.security.DigestInputStream
import java.security.MessageDigest

class ResourceLoader(private val entryClass: Class<*>) {
    fun load(resource: String): URL? {
        val fileResource = File(resource)
        return if (fileResource.exists()) {
            fileResource.toURI().toURL()
        } else {
            loadResource(resource)
        }
    }

    fun loadResource(resource: String): URL? {
        return entryClass.getResource(resource)
    }

    fun calculateVersion(resource: String): String {
        val md = MessageDigest.getInstance("MD5")
        val fileResource = File(resource)

        if (fileResource.exists()) {
            fileResource.inputStream()
        } else {
            entryClass.getResourceAsStream(resource)
        }.use { stream ->
            DigestInputStream(stream, md).use { dis -> dis.readAllBytes() }
        }
        return md.digest().toHexString()
    }
}
