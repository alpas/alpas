package dev.alpas.queue

import dev.alpas.Container
import dev.alpas.Environment

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
            failedQueueName = failedQueueName
        )
    }
}
