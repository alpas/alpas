package dev.alpas.ozone

import com.zaxxer.hikari.HikariDataSource
import dev.alpas.Environment
import org.jetbrains.exposed.sql.Database
import java.io.File
import java.nio.file.Paths

open class SqliteConnection(private val env: Environment, config: ConnectionConfig? = null) : DatabaseConnection {
    open val useNestedTransactions = config?.useNestedTransactions ?: false

    private val db by lazy {
        if (config?.database == ":memory:") {
            throw IllegalArgumentException("SQLite in-memory database is not supported.")
        } else {
            val database = (config?.database ?: defaultDatabase()).also(::createDatabaseFile)
            val ds = HikariDataSource().also {
                it.jdbcUrl = "jdbc:sqlite:$database"
            }
            Database.connect(ds).also { it.useNestedTransactions = useNestedTransactions }
        }
    }

    private fun defaultDatabase(): String {
        val db = "${env("DB_DATABASE", "dev")}.sqlite"
        return Paths.get(env.rootDir, "database", db).toUri().path
    }

    private fun createDatabaseFile(path: String) {
        File(path).apply {
            parentFile.mkdirs()
            createNewFile()
        }
    }

    override fun connect(): Database = db
}
