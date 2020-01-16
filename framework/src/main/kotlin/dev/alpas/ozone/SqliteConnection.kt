package dev.alpas.ozone

import com.zaxxer.hikari.HikariDataSource
import dev.alpas.Environment
import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.support.sqlite.SQLiteDialect
import java.io.File
import java.nio.file.Paths

open class SqliteConnection(private val env: Environment, config: ConnectionConfig? = null) : DatabaseConnection {
    open val dialect = config?.sqlDialect ?: SQLiteDialect()

    private val db by lazy {
        val database = if (config?.database == ":memory:") {
            ":memory:"
        } else {
            (config?.database ?: defaultDatabase()).also(::createDatabaseFile)
        }
        val ds = HikariDataSource().also { it.jdbcUrl = "jdbc:sqlite:$database" }
        Database.connect(ds, dialect)
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
