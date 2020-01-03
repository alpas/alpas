package dev.alpas.queue.database

import dev.alpas.Container
import dev.alpas.queue.JobHolder
import dev.alpas.queue.job.Job
import dev.alpas.queue.queueLogger
import dev.alpas.stackTraceString

class DatabaseJobHolder(
    private val job: Job,
    private val record: JobRecord,
    private val queue: DatabaseQueue
) : JobHolder {

    private val shouldRetry = record.tries < job.tries

    override fun commit() {
        queue.deleteJob(record)
        queueLogger.info { "Job ${record.id} successfully processed!" }
    }

    override fun rollback(ex: Exception) {
        queue.deleteJob(record)

        if (shouldRetry) {
            val tries = record.tries + 1
            queueLogger.warn { "Job ${record.id} processing failed. Tries so far: ${tries}." }
            queueLogger.error { ex.stackTraceString }
            queue.pushToDatabase(record.payload, record.queue, job.delayInSeconds, tries)
        } else {
            queueLogger.warn { "Job '${record.id}' has exceeded its retries count. Moving this job to the failed jobs queue." }
            queue.markAsFailedJob(record, ex)
        }
    }

    override fun process(container: Container) {
        job(container)
    }
}
