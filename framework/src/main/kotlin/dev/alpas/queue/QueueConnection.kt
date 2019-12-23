package dev.alpas.queue

import dev.alpas.Container

@Suppress("unused")

interface QueueConnection {
    fun connect(container: Container): Queue
}

open class ConnectionConfig(
    val username: String? = null,
    val password: String? = null,
    val host: String? = null,
    val port: String? = null,
    val defaultQueueName: String? = null,
    val failedQueueName: String? = null
)

