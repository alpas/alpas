package dev.alpas.queue.passthrough

import dev.alpas.Container
import dev.alpas.queue.JobHolder
import dev.alpas.queue.Queue
import dev.alpas.queue.job.Job

class PassThroughQueue(val container: Container) : Queue {
    override fun <T : Job> enqueue(job: T, onQueue: String?) {
        job(container)
    }

    override fun dequeue(from: String?): JobHolder? {
        TODO("Not Applicable")
    }
}
