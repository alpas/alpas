package dev.alpas.lodestar

import dev.alpas.Environment
import me.liuwj.ktorm.database.Database
import java.nio.file.Paths

open class SqliteConnection(private val env: Environment, config: ConnectionConfig? = null) : DatabaseConnection {
    private val db by lazy {
        val driver = "org.sqlite.JDBC"
        if (config?.database == ":memory:") {
            Database.connect("jdbc:sqlite:file:test?mode=memory&cache=shared", driver)
        } else {
            val database = config?.database ?: defaultDatabase()
            Database.connect("jdbc:sqlite:$database", driver)
        }
    }

    private fun defaultDatabase(): String {
        val db = "${env("DB_DATABASE", "dev")}.sqlite"
        return Paths.get(env.rootDir, "database", db).toString()
    }

    override fun connect(): Database = db
}
