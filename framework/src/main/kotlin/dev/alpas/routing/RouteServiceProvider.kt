package dev.alpas.routing

import dev.alpas.*
import dev.alpas.auth.AuthConfig
import dev.alpas.auth.console.MakeAuthCommand
import dev.alpas.console.Command
import dev.alpas.routing.console.*
import java.net.URI

@Suppress("unused")
open class RouteServiceProvider : ServiceProvider {
    override fun register(app: Application) {
        val router = Router()
        app.singleton(router)
        loadRoutes(router)
        app.registerAppHook(this)
    }

    protected open fun loadRoutes(router: Router) {}

    override fun commands(app: Application): List<Command> {
        val routeListCommand = RouteInfoCommand(app.srcPackage, app.make(), app.config { AuthConfig(app.make()) })
        return if (app.env.isDev) {
            return listOf(
                MakeControllerCommand(app.srcPackage),
                MakeAuthCommand(app.srcPackage),
                MakeMiddlewareCommand(app.srcPackage),
                routeListCommand,
                MakeValidationGuardCommand(app.srcPackage),
                MakeValidationRuleCommand(app.srcPackage)
            )
        } else {
            listOf(routeListCommand)
        }
    }

    override fun boot(app: Application, loader: PackageClassLoader) {
        app.logger.debug { "Compiling Router" }
        app.make<Router>().compile(loader)
    }

    override fun onAppStarted(app: Application, uri: URI) {
        val actualUri = if (app.env.isProduction) {
            URI(app.appConfig().appUrl)
        } else {
            uri
        }
        bindUrlGenerator(app, actualUri)
    }

    private fun bindUrlGenerator(container: Container, uri: URI) {
        // todo: re-initialize URL generator in dev mode
        container.singleton(UrlGenerator(uri, container.make(), container.make()))
    }
}
