package dev.alpas.routing

import dev.alpas.Application
import dev.alpas.PackageClassLoader
import dev.alpas.ServiceProvider
import dev.alpas.auth.AuthConfig
import dev.alpas.auth.console.MakeAuthCommand
import dev.alpas.config
import dev.alpas.console.Command
import dev.alpas.make
import dev.alpas.routing.console.MakeControllerCommand
import dev.alpas.routing.console.MakeMiddlewareCommand
import dev.alpas.routing.console.MakeValidationGuardCommand
import dev.alpas.routing.console.RouteListCommand

open class RouteServiceProvider : ServiceProvider {
    override fun register(app: Application) {
        app.singleton(Router())
    }

    override fun commands(app: Application): List<Command> {
        return listOf(
            MakeControllerCommand(app.srcPackage),
            MakeAuthCommand(app.srcPackage),
            MakeMiddlewareCommand(app.srcPackage),
            RouteListCommand(app.make(), app.config { AuthConfig(app.make()) }),
            MakeValidationGuardCommand(app.srcPackage)
        )
    }

    override fun boot(app: Application, loader: PackageClassLoader) {
        app.logger.debug { "Compiling Router" }
        app.make<Router>().compile(loader)
    }
}
