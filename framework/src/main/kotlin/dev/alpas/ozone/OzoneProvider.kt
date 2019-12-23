package dev.alpas.ozone

import dev.alpas.Application
import dev.alpas.ServiceProvider
import dev.alpas.config
import dev.alpas.console.Command
import dev.alpas.make
import dev.alpas.ozone.console.DatabaseCreateCommand
import dev.alpas.ozone.console.DatabaseMigrateCommand
import dev.alpas.ozone.console.DatabaseRefreshCommand
import dev.alpas.ozone.console.DatabaseRollbackCommand
import dev.alpas.ozone.console.MakeEntityCommand
import dev.alpas.ozone.console.MakeMigrationCommand

class OzoneProvider : ServiceProvider {
    override fun register(app: Application) {
        val dbConfig = app.config { DatabaseConfig(it.make()) }
        if (dbConfig.canConnect()) {
            dbConfig.connect()
        }
    }

    override fun commands(app: Application): List<Command> {
        val srcPackage = app.srcPackage
        return listOf(
            DatabaseMigrateCommand(srcPackage),
            DatabaseRollbackCommand(srcPackage),
            MakeEntityCommand(srcPackage),
            DatabaseCreateCommand(),
            MakeMigrationCommand(srcPackage),
            DatabaseRefreshCommand(srcPackage)
        )
    }
}
