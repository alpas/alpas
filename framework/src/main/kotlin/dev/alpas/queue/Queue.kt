package dev.alpas.queue

import dev.alpas.queue.job.Job
import mu.KotlinLogging

internal val queueLogger by lazy { KotlinLogging.logger("JobQueue") }

interface Queue {
    fun <T : Job> enqueue(job: T, onQueue: String? = null)
    fun dequeue(from: String? = null): JobHolder?
    fun close() {}
}

interface JobHolder {
    val job: Job
    fun commit()
    fun rollback(ex: Exception)
}
