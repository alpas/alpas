package dev.alpas.queue

import dev.alpas.Container
import dev.alpas.queue.job.Job
import mu.KotlinLogging
import java.time.Duration

internal val queueLogger by lazy { KotlinLogging.logger("JobQueue") }

interface Queue {
    fun <T : Job> enqueue(job: T, onQueue: String? = null)
    fun dequeue(from: String? = null, timeout: Duration? = null): JobHolder?
    fun close() {}
}

interface JobHolder {
    fun commit()
    fun rollback(ex: Exception)
    fun process(container: Container)
}
