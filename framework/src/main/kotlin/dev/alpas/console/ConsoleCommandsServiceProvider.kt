package dev.alpas.console

import dev.alpas.*

class ConsoleCommandsServiceProvider(private val args: Array<String>) : ServiceProvider {
    override fun register(app: Application) {
        app.bufferDebugLog("Registering $this")
        val alpas = AlpasCommand(app.appConfig().commandAliases)
        app.singleton(alpas)
    }

    override fun boot(app: Application) {
        if (app.env.runMode.isConsole()) {
            app.make<AlpasCommand>().execute(args)
        }
    }

    override fun commands(app: Application): List<Command> {
        return listOf(
            ServeCommand(app.makeElse { AppConfig(app.env) }),
            MakeCommandCommand(app.srcPackage),
            MakeServiceProviderCommand(app.srcPackage),
            KeyGenerateCommand(),
            LinkWebCommand(app.env, app.makeElse { AppConfig(app.env) })
        ) + app.kernel.commands(app)
    }
}
