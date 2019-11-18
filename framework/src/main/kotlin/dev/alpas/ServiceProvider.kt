package dev.alpas

import dev.alpas.console.Command

interface ServiceProvider {
    fun register(app: Application) {}
    fun register(app: Application, loader: PackageClassLoader) {
        register(app)
    }

    fun commands(app: Application) = emptyList<Command>()

    fun boot(app: Application) {}
    fun boot(app: Application, loader: PackageClassLoader) {
        boot(app)
    }

    fun stop(app: Application) {}
}
