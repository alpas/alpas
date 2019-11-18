package dev.alpas.lodestar.console

import dev.alpas.lodestar.DatabaseConfig
import dev.alpas.lodestar.migration.Connection
import dev.alpas.lodestar.migration.MigrationRunner
import java.io.File

class DatabaseMigrateCommand(dbConfig: DatabaseConfig, srcPackage: String) :
    MigrationCommand(dbConfig, srcPackage, name = "db:migrate", help = "Run the database migrations.") {

    override fun run() {
        MigrationRunner(
            File(migrationsDirectory.toUri()),
            Connection(dbConfig, dryRun),
            packageClassLoader
        ).migrate()
    }
}
