package dev.alpas.queue

interface Queue {
    fun <T : Job> enqueue(job: T, onQueue: String? = null, serializer: JobSerializer)
    fun dequeue(from: String? = null, serializer: JobSerializer, listener: (Job?) -> Unit)
    fun close() {}
}

interface JobSerializer {
    fun <T : Job> serialize(job: T): String
    fun deserialize(from: String): Job
}
