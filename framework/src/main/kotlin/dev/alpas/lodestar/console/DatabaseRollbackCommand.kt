package dev.alpas.lodestar.console

import dev.alpas.lodestar.DatabaseConfig
import dev.alpas.lodestar.migration.Connection
import dev.alpas.lodestar.migration.MigrationRunner
import java.io.File

class DatabaseRollbackCommand(dbConfig: DatabaseConfig, srcPackage: String) :
    MigrationCommand(dbConfig, srcPackage, name = "db:rollback", help = "Rollback the last database migration.") {

    override fun run() {
        MigrationRunner(
            File(migrationsDirectory.toUri()),
            Connection(dbConfig, dryRun),
            packageClassLoader
        ).rollback()
    }
}
