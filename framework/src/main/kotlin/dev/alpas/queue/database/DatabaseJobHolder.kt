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

        if (shouldRetry) {
            val tries = record.tries + 1
            if (job.onAttemptFailed(record, tries)) {
                queueLogger.warn {
                    "Job ${record.id} processing failed. Tries so far: ${tries}. Not requeuing because it has been handled"
                }
                queueLogger.error { ex.stackTraceString }
            } else {
                queueLogger.warn { "Job ${record.id} processing failed. Tries so far: ${tries}." }
                queueLogger.error { ex.stackTraceString }
                queue.putBack(record, job.delayInSeconds, tries)
            }
        } else {
            queue.deleteJob(record)
            if (job.onJobFailed(record)) {
                queueLogger.warn {
                    "Job '${record.id}' has exceeded its retries count but is handled. Not moving it to the failed job queue."
                }
            } else {
                queueLogger.warn { "Job '${record.id}' has exceeded its retries count. Moving this job to the failed jobs queue." }
                queue.markAsFailedJob(record, ex)
            }
        }
    }

    override suspend fun process(container: Container) {
        job(container)
    }

    override fun toString(): String {
        return "Database queue record ${record.id}; job $job"
    }

    override fun rollback() {
        queueLogger.info { "Rolling back $this" }
        queue.putBack(record, job.delayInSeconds, job.tries)
    }
}
