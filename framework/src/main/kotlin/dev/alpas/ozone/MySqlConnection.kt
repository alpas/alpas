package dev.alpas.ozone

import com.zaxxer.hikari.HikariDataSource
import dev.alpas.Environment
import me.liuwj.ktorm.database.Database

@Suppress("unused")
open class MySqlConnection(env: Environment, config: ConnectionConfig? = null) : DatabaseConnection {
    open val host = config?.host ?: env("DB_HOST", "localhost")
    open val port = config?.port ?: env("DB_PORT", 3306)
    open val database = config?.database ?: env("DB_DATABASE", "")
    open val username = config?.username ?: env("DB_USERNAME", "")
    open val password = config?.password ?: env("DB_PASSWORD", "")
    open val useSSL = config?.useSSL ?: env("DB_USE_SSL", false)
    open val dialect = config?.sqlDialect ?: MySqlDialect()
    open val serverTimezone = config?.serverTimezone ?: env("DB_TIMEZONE", "UTC")
    open val extraParams = config?.extraParams ?: emptyMap()
    internal val jdbcUrl by lazy {
        val params = combineParams(mapOf("useSSL" to useSSL, "serverTimezone" to "UTC") + extraParams)
        "jdbc:mysql://$host:$port/$database?$params"
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
