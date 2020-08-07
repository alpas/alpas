package dev.alpas.ozone.console

import dev.alpas.Container
import dev.alpas.ozone.migration.MigrationRunner
import java.io.File

class DatabaseRollbackCommand(private val container: Container, srcPackage: String) :
    MigrationCommand(srcPackage, name = "db:rollback", help = "Rollback the last database migration") {

    override fun run() {
        println()
        MigrationRunner(container, File(migrationsDirectory.toUri()), dryRun, migrationClassesScanner, useFilenames, quiet).rollback()
        withColors {
            println()
            echo(yellow("https://alpas.dev/docs/migrations"))
        }
    }
}
