package dev.alpas.queue

import dev.alpas.Container
import dev.alpas.queue.job.Job

@Suppress("unused")
open class QueueDispatcher(private val container: Container, private val config: QueueConfig) {
    open fun dispatch(job: Job, onQueue: String? = null, connection: String? = null) {
        config.connection(container, connection).enqueue(job, onQueue)
    }
}
