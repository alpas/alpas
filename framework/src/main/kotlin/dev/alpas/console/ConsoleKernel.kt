package dev.alpas.console

import dev.alpas.Application
import dev.alpas.Kernel

open class ConsoleKernel : Kernel {
    override fun boot(app: Application) {
        app.logger.debug { "Running your dev.alpas.console commands" }
        app.takeOff()
    }
}
