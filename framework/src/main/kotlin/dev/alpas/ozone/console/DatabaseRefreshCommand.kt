package dev.alpas.ozone.console

import dev.alpas.ozone.migration.DbAdapter
import dev.alpas.ozone.migration.MigrationRunner
import org.jetbrains.exposed.sql.Database
import java.io.File

class DatabaseRefreshCommand(srcPackage: String, db: Database) : MigrationCommand(
    srcPackage,
    name = "db:refresh",
    help = "Refresh a database by first dropping all the tables and then running all your migrations."
) {
    internal val adapter: DbAdapter by lazy {
        DbAdapter.make(dryRun, quiet, db)
    }

    override fun run() {
        adapter.dropAllTables()
        MigrationRunner(File(migrationsDirectory.toUri()), dryRun, packageClassLoader, quiet)
            .migrate()
    }
}
