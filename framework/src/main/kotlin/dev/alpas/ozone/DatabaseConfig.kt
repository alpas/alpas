package dev.alpas.ozone

import dev.alpas.Config
import dev.alpas.Environment
import me.liuwj.ktorm.database.Database

open class DatabaseConfig(val env: Environment) : Config {
    private val connections = mutableMapOf<String, Lazy<DatabaseConnection>>()
    open val defaultConnection = env("DB_CONNECTION", "mysql")

    protected fun addConnection(key: String, connection: Lazy<DatabaseConnection>) {
        connections[key] = connection
    }

    fun canConnect(): Boolean {
        return connections.isNotEmpty()
    }

    open fun connect(connection: String = defaultConnection): Database {
        return connections[connection]?.value?.connect()
            ?: throw Exception("Unsupported database connection: '$connection'.")
    }

    open fun reconnect(connection: String = defaultConnection): Database {
        return connections[connection]?.value?.reconnect()
            ?: throw Exception("Unsupported database connection: '$connection'.")
    }
}
