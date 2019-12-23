package dev.alpas.queue.activemq

import dev.alpas.Container
import dev.alpas.Environment
import dev.alpas.make
import dev.alpas.queue.ConnectionConfig
import dev.alpas.queue.Queue
import dev.alpas.queue.QueueConnection
import dev.alpas.queue.jms.JMSQueue

@Suppress("unused")
open class ActiveMQConnection(env: Environment, config: ConnectionConfig? = null) :
    QueueConnection {
    open val queueNamespace = env("APP_NAME")?.toLowerCase()
    open val username = config?.username ?: env("ACTIVEMQ_USERNAME", "")
    open val password = config?.password ?: env("ACTIVEMQ_PASSWORD", "")
    open val host = config?.host ?: env("ACTIVEMQ_HOST", "localhost")
    open val port = config?.port ?: env("ACTIVEMQ_PORT", 3306)
    open val defaultQueueName =
        config?.defaultQueueName ?: env("ACTIVEMQ_DEFAULT_QUEUE_NAME", "$queueNamespace::default")
    open val failedQueueName =
        config?.failedQueueName ?: env("ACTIVEMQ_FAILED_QUEUE_NAME", "$queueNamespace::failed")


    override fun connect(container: Container): Queue {
        return JMSQueue(
            "amqp://$host:$port",
            username = username,
            password = password,
            defaultQueueName = defaultQueueName,
            failedQueueName = failedQueueName,
            serializer = container.make()
        )
    }
}
