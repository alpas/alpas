package dev.alpas.queue

open class QueueConnectionConfig(val name: String, val queue: Queue? = null) {
    companion object {
        fun activeMQ(
            username: String,
            password: String,
            defaultQueueName: String,
            failedQueueName: String = "DLQ",
            host: String = "localhost",
            port: Int = 5672
        ): QueueConnectionConfig {
            return QueueConnectionConfig(
                name = "activemq",
                queue = JMSQueue(
                    "amqp://$host:$port",
                    username = username,
                    password = password,
                    defaultQueueName = defaultQueueName,
                    failedQueueName = failedQueueName
                )
            )
        }

        fun sync(): QueueConnectionConfig {
            return QueueConnectionConfig(name = "sync")
        }
    }
}
