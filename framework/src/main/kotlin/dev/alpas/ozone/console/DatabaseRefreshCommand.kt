package dev.alpas.ozone.console

import dev.alpas.ozone.migration.DbAdapter
import dev.alpas.ozone.migration.MigrationRunner
import java.io.File

class DatabaseRefreshCommand(srcPackage: String) : MigrationCommand(
    srcPackage,
    name = "db:refresh",
    help = "Refresh a database by first dropping all the tables and then running all your migrations"
) {
    internal val adapter: DbAdapter by lazy {
        DbAdapter.make(dryRun, quiet)
    }

    override fun run() {
        adapter.dropAllTables()
        MigrationRunner(File(migrationsDirectory.toUri()), dryRun, packageClassLoader, quiet)
            .migrate()
        withColors {
            echo(yellow("https://alpas.dev/docs/migrations"))
        }
    }
}
