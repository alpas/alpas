package dev.alpas.ozone.console

import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import dev.alpas.Application
import dev.alpas.asMagenta
import dev.alpas.ozone.migration.DbAdapter
import dev.alpas.ozone.migration.MigrationRunner
import me.liuwj.ktorm.database.Database
import java.io.File

class DatabaseRefreshCommand(private val app: Application) : MigrationCommand(
    app.srcPackage,
    name = "db:refresh",
    help = "Refresh the database ${Database.global.name.asMagenta()} by first dropping all its tables and then running all your migrations"
) {
    private val seed by option("--seed", "-s", help = "Seed the database after refreshing it").flag()

    internal val adapter: DbAdapter by lazy {
        DbAdapter.make(dryRun, quiet)
    }

    override fun run() {
        println()
        adapter.dropAllTables()
        println()
        MigrationRunner(File(migrationsDirectory.toUri()), dryRun, migrationClassesScanner, useFilenames, quiet)
            .migrate()
        withColors {
            println()
            echo(yellow("https://alpas.dev/docs/migrations"))
        }

        if (seed) {
            val options = if (quiet) arrayOf("--quiet") else emptyArray()
            DatabaseSeedCommand(app).main(options)
        }
    }
}
