package dev.alpas.queue

import dev.alpas.Container

class SyncQueue(val container: Container) : Queue {
    override fun <T : Job> enqueue(job: T, onQueue: String?, serializer: JobSerializer) {
        job.invoke(container)
    }

    override fun dequeue(from: String?, serializer: JobSerializer, listener: (Job?) -> Unit) {
    }
}
