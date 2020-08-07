package dev.alpas.ozone.console

import dev.alpas.Container
import dev.alpas.ozone.migration.MigrationRunner
import java.io.File

class DatabaseMigrateCommand(private val container: Container, srcPackage: String) :
    MigrationCommand(srcPackage, name = "db:migrate", help = "Run the database migrations") {

    override fun run() {
        println()
        MigrationRunner(container, File(migrationsDirectory.toUri()), dryRun, migrationClassesScanner, useFilenames, quiet).migrate()
        withColors {
            println()
            echo(yellow("https://alpas.dev/docs/migrations"))
        }
    }
}
