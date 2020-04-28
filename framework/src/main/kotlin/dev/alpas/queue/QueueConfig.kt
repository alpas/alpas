package dev.alpas.queue

import dev.alpas.Config
import dev.alpas.Container
import dev.alpas.Environment

@Suppress("unused")
open class QueueConfig(private val env: Environment) : Config {
    private val connections = mutableMapOf<String, Lazy<QueueConnection>>()
    open val defaultConnection = env("QUEUE_CONNECTION", "activemq")
    open val queueRestartTripPath = triggerPath(env("QUEUE_RESTART_TRIP_PATH"))

    private fun triggerPath(envPath: String?): String {
        val path = envPath ?: env.storagePath("app")
        return "$path/alpas_queue_trigger"
    }

    fun addConnection(key: String, connection: Lazy<QueueConnection>) {
        connections[key] = connection
    }

    fun connection(container: Container, name: String? = null): Queue {
        val connectionName = name ?: defaultConnection
        return connections[connectionName]?.value?.connect(container)
            ?: throw Exception("Unsupported queue connection: '$connectionName'.")
    }

    internal fun registeredConnectionNames(): List<String> {
        return connections.keys.toList()
    }

    fun checkPrerequisites(name: String): Boolean {
        return connections[name]?.value?.checkPrerequisites() ?: true
    }
}
