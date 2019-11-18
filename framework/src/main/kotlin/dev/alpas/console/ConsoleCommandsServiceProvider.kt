package dev.alpas.console

import dev.alpas.Application
import dev.alpas.ServiceProvider
import dev.alpas.appConfig
import dev.alpas.make

class ConsoleCommandsServiceProvider(private val args: Array<String>) : ServiceProvider {
    override fun register(app: Application) {
        app.bufferDebugLog("Registering $this")
        val alpas = AlpasCommand(app.appConfig().topLevelCommandAliases)
        app.singleton(alpas)
    }

    override fun boot(app: Application) {
        if (app.env.runMode.isConsole()) {
            app.make<AlpasCommand>().execute(args)
        }
    }

    override fun commands(app: Application): List<Command> {
        return listOf(
            ServeCommand(),
            MakeCommandCommand(app.srcPackage),
            MakeServiceProviderCommand(app.srcPackage),
            KeyGenerateCommand()
        )
    }
}
