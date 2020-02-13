package dev.alpas.ozone

import dev.alpas.Application
import dev.alpas.ServiceProvider
import dev.alpas.console.Command
import dev.alpas.make
import dev.alpas.makeElse
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
                DatabaseMigrateCommand(srcPackage),
                DatabaseRefreshCommand(app),
                DatabaseRollbackCommand(srcPackage),
                DatabaseSeedCommand(app)
            ).plus(coreOzoneCommands)
        } else {
            coreOzoneCommands
        }
    }
}
