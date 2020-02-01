package dev.alpas.console

import dev.alpas.Application
import dev.alpas.Kernel
import dev.alpas.ServiceProvider
import dev.alpas.encryption.EncryptionServiceProvider
import dev.alpas.hashing.HashServiceProvider
import dev.alpas.logging.LoggerServiceProvider
import dev.alpas.make
import dev.alpas.ozone.OzoneServiceProvider
import dev.alpas.routing.RouteServiceProvider
import kotlin.reflect.KClass

open class ConsoleKernel : Kernel {
    lateinit var args: Array<String>

    override fun capture(args: Array<String>) {
        this.args = args
    }

    override fun boot(app: Application) {
        app.logger.debug { "Running your dev.alpas.console commands" }
        app.takeOff()
        if (app.env.runMode.isConsole()) {
            app.make<AlpasCommand>().execute(args)
        }
    }

    override fun serviceProviders(app: Application): Iterable<KClass<out ServiceProvider>> {
        return listOf(
            LoggerServiceProvider::class,
            EncryptionServiceProvider::class,
            HashServiceProvider::class,
            RouteServiceProvider::class,
            OzoneServiceProvider::class
        )
    }
}
