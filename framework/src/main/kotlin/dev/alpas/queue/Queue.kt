package dev.alpas.queue

import dev.alpas.queue.job.Job

interface Queue {
    fun <T : Job> enqueue(job: T, onQueue: String? = null)
    fun dequeue(from: String? = null, listener: (Job?) -> Unit)
    fun close() {}
}
