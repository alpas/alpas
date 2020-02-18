package dev.alpas.ozone.console

import dev.alpas.ozone.migration.MigrationRunner
import java.io.File

class DatabaseRollbackCommand(srcPackage: String) :
    MigrationCommand(srcPackage, name = "db:rollback", help = "Rollback the last database migration") {

    override fun run() {
        println()
        MigrationRunner(File(migrationsDirectory.toUri()), dryRun, packageClassLoader, useNames, quiet).rollback()
        withColors {
            println()
            echo(yellow("https://alpas.dev/docs/migrations"))
        }
    }
}
