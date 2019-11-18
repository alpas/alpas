package dev.alpas.ozone

import dev.alpas.Environment
import me.liuwj.ktorm.database.Database

open class MySqlConnection(env: Environment, config: ConnectionConfig? = null) : DatabaseConnection {
    open val host = config?.host ?: env("DB_HOST", "localhost")
    open val port = config?.port ?: env("DB_PORT", 3306)
    open val database = config?.database ?: env("DB_DATABASE", "")
    open val username = config?.username ?: env("DB_USERNAME", "")
    open val password = config?.password ?: env("DB_PASSWORD", "")
    open val useSSL = config?.useSSL ?: env("DB_USE_SSL", false)
    open val dialect = config?.sqlDialect

    private val db: Database by lazy {
        val url = "jdbc:mysql://$host:$port/$database?useSSL=${useSSL}"
        val driver = "com.mysql.jdbc.Driver"
        Database.connect(url, driver, username, password, dialect = dialect)
    }

    override fun connect(): Database = db
}
