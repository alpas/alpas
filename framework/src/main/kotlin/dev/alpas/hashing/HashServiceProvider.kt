package dev.alpas.hashing

import dev.alpas.Application
import dev.alpas.ServiceProvider
import dev.alpas.config

class HashServiceProvider : ServiceProvider {
    override fun register(app: Application) {
        val config = app.config {
            HashConfig()
        }
        app.singleton(Hasher(config))
    }
}
