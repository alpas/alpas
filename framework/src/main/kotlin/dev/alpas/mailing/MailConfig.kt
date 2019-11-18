package dev.alpas.mailing

import dev.alpas.Config
import dev.alpas.Environment
import dev.alpas.mailing.drivers.MailDriver

open class MailConfig(env: Environment) : Config {
    open val defaultDriver = env("MAIL_DRIVER", "smtp")
    private val drivers = mutableMapOf<String, Lazy<MailDriver>>()

    fun addDriver(name: String, driver: Lazy<MailDriver>) {
        drivers[name] = driver
    }

    fun driver(name: String = defaultDriver): MailDriver {
        return drivers[name]?.value ?: throw Exception("Unsupported mail driver: '$name'.")
    }
}
