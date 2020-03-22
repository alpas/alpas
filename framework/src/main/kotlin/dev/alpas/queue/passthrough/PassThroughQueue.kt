package dev.alpas.queue.passthrough

import dev.alpas.Container
import dev.alpas.queue.JobHolder
import dev.alpas.queue.Queue
import dev.alpas.queue.job.Job
import java.time.Duration

class PassThroughQueue(val container: Container) : Queue {
    override fun <T : Job> enqueue(job: T, onQueue: String?) {
        suspend {
            job(container)
        }
    }

    override suspend fun dequeue(from: String?, timeout: Duration?): JobHolder? {
        TODO("Not Applicable")
    }
}
