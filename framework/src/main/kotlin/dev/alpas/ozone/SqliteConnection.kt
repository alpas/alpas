package dev.alpas.ozone

import dev.alpas.Environment
import me.liuwj.ktorm.database.Database
import java.io.File
import java.nio.file.Paths

open class SqliteConnection(private val env: Environment, config: ConnectionConfig? = null) : DatabaseConnection {
    open val dialect = config?.sqlDialect

    private val db by lazy {
        val driver = "org.sqlite.JDBC"
        if (config?.database == ":memory:") {
            throw IllegalArgumentException("SQLite in-memory database is not supported.")
        } else {
            val database = (config?.database ?: defaultDatabase()).also(::createDatabaseFile)
            Database.connect("jdbc:sqlite:$database", driver, dialect = dialect)
        }
    }

    private fun defaultDatabase(): String {
        val db = "${env("DB_DATABASE", "dev")}.sqlite"
        return Paths.get(env.rootDir, "database", db).toString()
    }

    private fun createDatabaseFile(path: String) {
        File(path).apply {
            parentFile.mkdirs()
            createNewFile()
        }
    }

    override fun connect(): Database = db
}
