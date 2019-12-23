package dev.alpas.queue.database

import dev.alpas.Container
import dev.alpas.Environment
import dev.alpas.make
import dev.alpas.queue.ConnectionConfig
import dev.alpas.queue.Queue
import dev.alpas.queue.QueueConnection

@Suppress("unused")
open class DatabaseQueueConnection(env: Environment, config: ConnectionConfig? = null) : QueueConnection {
    open val defaultQueueName = config?.defaultQueueName ?: env("DB_DEFAULT_QUEUE_NAME", "default")
    override fun connect(container: Container): Queue {
        return DatabaseQueue(defaultQueueName, container.make())
    }
}
