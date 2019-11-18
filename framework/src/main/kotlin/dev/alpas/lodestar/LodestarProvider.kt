package dev.alpas.lodestar

import dev.alpas.Application
import dev.alpas.ServiceProvider
import dev.alpas.config
import dev.alpas.console.Command
import dev.alpas.lodestar.console.DatabaseCreateCommand
import dev.alpas.lodestar.console.DatabaseMigrateCommand
import dev.alpas.lodestar.console.DatabaseRollbackCommand
import dev.alpas.lodestar.console.MakeEntityCommand
import dev.alpas.lodestar.console.MakeMigrationCommand
import dev.alpas.make

open class LodestarProvider : ServiceProvider {
    override fun register(app: Application) {
        val dbConfig = app.config { DatabaseConfig(it.make()) }
        if (dbConfig.hasAnyConnection()) {
            dbConfig.connect()
        }
    }

    override fun commands(app: Application): List<Command> {
        val dbConfig = app.config<DatabaseConfig>()
        val srcPackage = app.srcPackage
        return listOf(
            DatabaseMigrateCommand(dbConfig, srcPackage),
            DatabaseRollbackCommand(dbConfig, srcPackage),
            MakeEntityCommand(srcPackage),
            DatabaseCreateCommand(dbConfig),
            MakeMigrationCommand(srcPackage)
        )
    }
}
