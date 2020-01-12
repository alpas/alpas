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
        val config = app.make<DatabaseConfig>()
        return if (config.canConnect()) {
            val db = config.connect()
            val srcPackage = app.srcPackage
            listOf(
                DatabaseMigrateCommand(srcPackage),
                DatabaseRollbackCommand(srcPackage),
                MakeEntityCommand(srcPackage),
                DatabaseCreateCommand(db),
                MakeMigrationCommand(srcPackage),
                DatabaseRefreshCommand(srcPackage, db)
            )
        } else {
            emptyList()
        }
    }
}
