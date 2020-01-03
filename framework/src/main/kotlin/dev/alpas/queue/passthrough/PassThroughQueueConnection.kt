package dev.alpas.queue.passthrough

import dev.alpas.Container
import dev.alpas.queue.Queue
import dev.alpas.queue.QueueConnection

@Suppress("unused")
class PassThroughQueueConnection : QueueConnection {
    override fun connect(container: Container): Queue {
        return PassThroughQueue(container)
    }
}
