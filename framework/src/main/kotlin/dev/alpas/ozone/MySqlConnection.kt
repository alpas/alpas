package dev.alpas.ozone

import com.zaxxer.hikari.HikariDataSource
import dev.alpas.Environment
import org.jetbrains.exposed.sql.Database

@Suppress("unused")
open class MySqlConnection(env: Environment, config: ConnectionConfig? = null) : DatabaseConnection {
    open val host = config?.host ?: env("DB_HOST", "localhost")
    open val port = config?.port ?: env("DB_PORT", 3306)
    open val database = config?.database ?: env("DB_DATABASE", "")
    open val username = config?.username ?: env("DB_USERNAME", "")
    open val password = config?.password ?: env("DB_PASSWORD", "")
    open val useSSL = config?.useSSL ?: env("DB_USE_SSL", false)
    open val useNestedTransactions = config?.useNestedTransactions ?: false

    private val db: Database by lazy {
        val ds = HikariDataSource().also {
            it.jdbcUrl = "jdbc:mysql://$host:$port/$database?useSSL=${useSSL}"
            it.username = username
            it.password = password
        }
        Database.connect(ds).also { it.useNestedTransactions = useNestedTransactions }
    }

    override fun connect(): Database = db
}
