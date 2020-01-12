package dev.alpas.runner.configs

import dev.alpas.Environment
import dev.alpas.ozone.ConnectionConfig
import dev.alpas.ozone.DatabaseConfig
import dev.alpas.ozone.MySqlConnection

@Suppress("unused")
class DatabaseConfig(env: Environment) : DatabaseConfig(env) {
    init {
        addConnection(
            "mysql",
            lazy { MySqlConnection(env, ConnectionConfig()) }
        )
    }
}
