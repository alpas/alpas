package dev.alpas.ozone

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.alpas.Environment
import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.support.sqlite.SQLiteDialect
import java.io.File

@Suppress("unused")
open class SqliteConnection(private val env: Environment, private val config: ConnectionConfig? = null) : DatabaseConnection {
    open val dialect = config?.sqlDialect ?: SQLiteDialect()
    open val extraParams = config?.extraParams ?: emptyMap()

    private val db by lazy {
        makeConnection()
    }

    private val dataSource by lazy {
        val database = if (config?.database == ":memory:") {
            ":memory:"
        } else {
            (config?.database ?: defaultDatabase()).also(::createDatabaseFile)
        }
        val params = combineParams(extraParams)
        val config = HikariConfig().apply {
            jdbcUrl = "jdbc:sqlite:$database?$params"
            driverClassName = "org.sqlite.JDBC"
        }
        HikariDataSource(config)
    }

    private fun makeConnection(): Database {
        return Database.connect(dataSource, dialect)
    }

    private fun defaultDatabase(): String {
        val db = "${env("DB_DATABASE", "dev")}.sqlite"
        return env.rootPath("database", db)
    }

    private fun createDatabaseFile(path: String) {
        File(path).apply {
            parentFile.mkdirs()
            createNewFile()
        }
    }

    override fun connect(): Database = db

    override fun disconnect() {
        if (!dataSource.isClosed) {
            dataSource.close()
        }
    }

    override fun reconnect(): Database = makeConnection()
}
