package dev.alpas.queue

import dev.alpas.Container

@Suppress("unused")
class SyncQueueConnection : QueueConnection {
    override fun connect(container: Container): Queue {
        return SyncQueue(container)
    }
}
