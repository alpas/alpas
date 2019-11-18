package dev.alpas.encryption

import dev.alpas.Application
import dev.alpas.ServiceProvider
import dev.alpas.appConfig
import dev.alpas.base64Decoded
import dev.alpas.singletonFactory

class EncryptionServiceProvider : ServiceProvider {
    override fun register(app: Application) {
        val config = app.appConfig()
        var encryptionKey = config.encryptionKey
            ?: throw Exception("APP_KEY is empty. Set the value in your .env file")
        if (encryptionKey.startsWith("base64:")) {
            encryptionKey = encryptionKey.substring(7).base64Decoded()
        }
        app.singletonFactory {
            Encrypter(encryptionKey)
        }
    }
}

