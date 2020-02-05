package dev.alpas.ozone

import com.zaxxer.hikari.HikariDataSource
import dev.alpas.Environment
import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.support.postgresql.PostgreSqlDialect

@Suppress("unused")
open class PostgreSqlConnection(env: Environment, config: ConnectionConfig? = null) : DatabaseConnection {
    open val host = config?.host ?: env("DB_HOST", "localhost")
    open val port = config?.port ?: env("DB_PORT", 5432)
    open val database = config?.database ?: env("DB_DATABASE", "")
    open val username = config?.username ?: env("DB_USERNAME", "")
    open val password = config?.password ?: env("DB_PASSWORD", "")
    open val dialect = config?.sqlDialect ?: PostgreSqlDialect()
    open val extraParams = config?.extraParams ?: emptyMap()
    internal val jdbcUrl by lazy {
        val params = combineParams(extraParams)
        "jdbc:postgresql://$host:$port/$database?$params"
    }

    private val db: Database by lazy {
        val ds = HikariDataSource().also {
            it.jdbcUrl = jdbcUrl
            it.username = username
            it.password = password
        }
        Database.connect(ds, dialect)
    }

    override fun connect(): Database = db
}
