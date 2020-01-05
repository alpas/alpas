package dev.alpas.console

import dev.alpas.Application
import dev.alpas.Kernel
import dev.alpas.ServiceProvider
import dev.alpas.encryption.EncryptionServiceProvider
import dev.alpas.hashing.HashServiceProvider
import dev.alpas.logging.LoggerServiceProvider
import dev.alpas.routing.RouteServiceProvider
import kotlin.reflect.KClass

open class ConsoleKernel : Kernel {
    override fun boot(app: Application) {
        app.logger.debug { "Running your dev.alpas.console commands" }
        app.takeOff()
    }

    override fun serviceProviders(app: Application): Iterable<KClass<out ServiceProvider>> {
        return listOf(
            LoggerServiceProvider::class,
            RouteServiceProvider::class,
            EncryptionServiceProvider::class,
            HashServiceProvider::class
        )
    }
}
