package dev.alpas.ozone

import me.liuwj.ktorm.database.SqlDialect

open class ConnectionConfig(
    val username: String? = null,
    val password: String? = null,
    val host: String? = null,
    val port: String? = null,
    val database: String? = null,
    val useSSL: Boolean = false,
    val sqlDialect: SqlDialect? = null
)
