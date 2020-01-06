package dev.alpas.ozone.console

import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import dev.alpas.console.Command
import dev.alpas.ozone.migration.DbAdapter
import org.jetbrains.exposed.sql.Database

class DatabaseCreateCommand(db: Database) : Command(name = "db:create", help = "Create a database.") {
    private val names by argument().multiple()
    protected val dryRun by option("--dry-run", help = "Don't execute the query but only print it.").flag()

    internal val adapter: DbAdapter by lazy { DbAdapter.make(dryRun, quiet, db) }

    override fun run() {
        names.forEach {
            warning("Creating database")
            if (adapter.createDatabase(it)) {
                success("Created database: $it")
            }
        }
    }
}
