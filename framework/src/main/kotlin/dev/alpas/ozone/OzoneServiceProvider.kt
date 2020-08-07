package dev.alpas.ozone

import dev.alpas.*
import dev.alpas.console.Command
import dev.alpas.ozone.console.*

class OzoneServiceProvider : ServiceProvider {
    override fun register(app: Application) {
        val dbConfig = app.makeElse { bind(DatabaseConfig(it.make())) }
        if (dbConfig.canConnect()) {
            dbConfig.connect()
        }
    }

    override fun commands(app: Application): List<Command> {
        val srcPackage = app.srcPackage
        val coreOzoneCommands =
            if (app.env.isDev) {
                listOf(
                    MakeEntityCommand(srcPackage),
                    MakeFactoryCommand(srcPackage),
                    MakeMigrationCommand(srcPackage),
                    MakeSeederCommand(srcPackage)
                )
            } else {
                emptyList()
            }
        val config = app.make<DatabaseConfig>()
        return if (config.canConnect()) {
            listOf(
                DatabaseCreateCommand(),
                DatabaseMigrateCommand(app, srcPackage),
                DatabaseRefreshCommand(app),
                DatabaseRollbackCommand(app, srcPackage),
                DatabaseSeedCommand(app)
            ).plus(coreOzoneCommands)
        } else {
            coreOzoneCommands
        }
    }

    override fun stop(app: Application) {
        app.tryMake<DatabaseConfig>()?.connection()?.disconnect()
    }
}
