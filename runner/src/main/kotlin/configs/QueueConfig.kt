package dev.alpas.runner.configs

import dev.alpas.Environment
import dev.alpas.queue.activemq.ActiveMQConnection
import dev.alpas.queue.passthrough.PassThroughQueueConnection
import dev.alpas.queue.QueueConfig as BaseConfig

@Suppress("unused")
class QueueConfig(env: Environment) : BaseConfig(env) {
    init {
        addConnection("activemq", lazy { ActiveMQConnection(env) })
        addConnection("sync", lazy { PassThroughQueueConnection() })
    }
}
