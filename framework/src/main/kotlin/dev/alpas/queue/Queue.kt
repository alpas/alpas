package dev.alpas.queue

import javax.jms.JMSProducer

interface Queue {
    fun <T : Job> enqueue(
        job: T,
        onQueue: String? = null,
        serializer: JobSerializer,
        setProperties: (JMSProducer.() -> Unit) = { }
    )

    fun dequeue(from: String? = null, serializer: JobSerializer, listener: (Job?) -> Unit)
    fun close() {}
}

interface JobSerializer {
    fun <T : Job> serialize(job: T): String
    fun deserialize(from: String): Job
}
