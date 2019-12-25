package dev.alpas.queue

import dev.alpas.Config
import dev.alpas.Container
import dev.alpas.Environment

@Suppress("unused")
open class QueueConfig(env: Environment) : Config {
    private val connections = mutableMapOf<String, Lazy<QueueConnection>>()
    open val defaultConnection = env("QUEUE_CONNECTION", "activemq")

    fun addConnection(key: String, connection: Lazy<QueueConnection>) {
        connections[key] = connection
    }

    fun canConnect(): Boolean {
        return connections.isNotEmpty()
    }

    fun connection(container: Container, name: String? = null): Queue {
        val connectionName = name ?: defaultConnection
        return connections[connectionName]?.value?.connect(container)
            ?: throw Exception("Unsupported queue connection: '$connectionName'.")
    }

    internal fun registeredConnectionNames(): List<String> {
        return connections.keys.toList()
    }
}
