package dev.alpas.console

import dev.alpas.*
import dev.alpas.encryption.EncryptionServiceProvider
import dev.alpas.hashing.HashServiceProvider
import dev.alpas.logging.LoggerServiceProvider
import dev.alpas.ozone.OzoneServiceProvider
import dev.alpas.routing.RouteServiceProvider
import java.net.URI
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
            val config = app.config<AppConfig>()
            app.inFlight(URI(config.appUrl))
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
