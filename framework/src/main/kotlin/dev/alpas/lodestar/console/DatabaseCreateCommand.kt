package dev.alpas.lodestar.console

import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import dev.alpas.console.Command
import dev.alpas.lodestar.DatabaseConfig
import dev.alpas.lodestar.migration.Connection

class DatabaseCreateCommand(private val dbConfig: DatabaseConfig) :
    Command(name = "db:create", help = "Create a database.") {
    private val names by argument().multiple()

    override fun run() {
        val adapter = Connection(dbConfig).adapter
        names.forEach {
            warning("Creating database: $it...")
            adapter.createDatabase(it)
            success("Created database: $it")
        }
    }
}
