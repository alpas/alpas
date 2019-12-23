package dev.alpas.queue.sync

import dev.alpas.Container
import dev.alpas.queue.Queue
import dev.alpas.queue.job.Job

class SyncQueue(val container: Container) : Queue {
    override fun <T : Job> enqueue(job: T, onQueue: String?) {
        job.invoke(container)
    }

    override fun dequeue(from: String?, listener: (Job?) -> Unit) {
    }
}
