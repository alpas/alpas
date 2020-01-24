package dev.alpas.ozone.console

import dev.alpas.ozone.migration.MigrationRunner
import java.io.File

class DatabaseMigrateCommand(srcPackage: String) :
    MigrationCommand(srcPackage, name = "db:migrate", help = "Run the database migrations") {

    override fun run() {
        MigrationRunner(File(migrationsDirectory.toUri()), dryRun, packageClassLoader, quiet).migrate()
        withColors {
            echo(yellow("https://alpas.dev/docs/migrations"))
        }
    }
}
