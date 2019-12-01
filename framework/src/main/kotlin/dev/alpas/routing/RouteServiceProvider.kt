package dev.alpas.routing

import dev.alpas.*
import dev.alpas.auth.AuthConfig
import dev.alpas.auth.console.MakeAuthCommand
import dev.alpas.console.Command
import dev.alpas.routing.console.*

@Suppress("unused")
open class RouteServiceProvider : ServiceProvider {
    override fun register(app: Application) {
        val router = Router()
        app.singleton(router)
        loadRoutes(router)
    }

    protected open fun loadRoutes(router: Router) {
    }

    override fun commands(app: Application): List<Command> {
        return listOf(
            MakeControllerCommand(app.srcPackage),
            MakeAuthCommand(app.srcPackage),
            MakeMiddlewareCommand(app.srcPackage),
            RouteListCommand(app.make(), app.config { AuthConfig(app.make()) }),
            MakeValidationGuardCommand(app.srcPackage),
            MakeValidationRuleCommand(app.srcPackage)
        )
    }

    override fun boot(app: Application, loader: PackageClassLoader) {
        app.logger.debug { "Compiling Router" }
        app.make<Router>().compile(loader)
    }
}
