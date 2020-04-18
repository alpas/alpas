package dev.alpas.mailing

import dev.alpas.Config
import dev.alpas.Environment
import dev.alpas.RESOURCES_DIRS
import dev.alpas.mailing.drivers.MailDriver

open class MailConfig(env: Environment) : Config {
    open val defaultDriver = env("MAIL_DRIVER", "local")
    open val templatesPath = env.storagePath(*RESOURCES_DIRS, "templates")
    private val drivers = mutableMapOf<String, Lazy<MailDriver>>()

    fun addDriver(name: String, driver: Lazy<MailDriver>) {
        drivers[name] = driver
    }

    fun driver(name: String? = null): MailDriver {
        val driverName = name ?: defaultDriver
        return drivers[driverName]?.value ?: throw Exception("Unsupported mail driver: '$driverName'.")
    }

    fun renderer(): EmailRenderer {
        return EmailRenderer(templatesPath)
    }
}
