package dev.alpas.queue

import dev.alpas.Config
import dev.alpas.Environment

open class QueueConfig(env: Environment) : Config {
    private val connectionConfigs = mutableMapOf<String, QueueConnectionConfig>()
    protected open val defaultConnection = env("QUEUE_CONNECTION", "activemq")
    fun addConnection(connectionConfig: QueueConnectionConfig) {
        connectionConfigs.putIfAbsent(connectionConfig.name, connectionConfig)
    }

    fun connection(name: String = defaultConnection): Queue {
        return connectionConfigs[name]?.queue ?: throw Exception("No such connection $name")
    }
}
