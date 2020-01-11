package dev.alpas.ozone

import dev.alpas.Application
import dev.alpas.ServiceProvider
import dev.alpas.console.Command
import dev.alpas.make
import dev.alpas.makeElse
import dev.alpas.ozone.console.*

class OzoneProvider : ServiceProvider {
    override fun register(app: Application) {
        val dbConfig = app.makeElse { DatabaseConfig(it.make()) }
        if (dbConfig.canConnect()) {
            dbConfig.connect()
        }
    }

    override fun commands(app: Application): List<Command> {
        val db = app.make<DatabaseConfig>().connect()
        val srcPackage = app.srcPackage
        return listOf(
            DatabaseMigrateCommand(srcPackage),
            DatabaseRollbackCommand(srcPackage),
            MakeEntityCommand(srcPackage),
            DatabaseCreateCommand(db),
            MakeMigrationCommand(srcPackage),
            DatabaseRefreshCommand(srcPackage, db)
        )
    }
}
