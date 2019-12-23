package dev.alpas.queue.sync

import dev.alpas.Container
import dev.alpas.queue.Queue
import dev.alpas.queue.QueueConnection

@Suppress("unused")
class SyncQueueConnection : QueueConnection {
    override fun connect(container: Container): Queue {
        return SyncQueue(container)
    }
}
