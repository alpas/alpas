package dev.alpas.queue.jms

import dev.alpas.Container
import dev.alpas.queue.JobHolder
import dev.alpas.queue.job.Job
import dev.alpas.queue.queueLogger
import dev.alpas.stackTraceString
import javax.jms.JMSContext
import javax.jms.Message

open class JMSJobHolder(
    private val job: Job,
    private val context: JMSContext,
    private val message: Message,
    private val queueName: String,
    private val payload: String,
    private val queue: JMSQueue
) : JobHolder {

    override fun commit() {
        context.commit()
        context.close()
    }

    private val tries = message.getIntProperty("JMSXDeliveryCount")
    private val shouldRetry = tries < job.tries

    override fun rollback(ex: Exception) {
        val jobId = message.jmsMessageID
        if (shouldRetry) {
            queueLogger.warn { "Job $jobId processing failed. Tries so far: ${tries}." }
            queueLogger.error { ex.stackTraceString }
            context.rollback()
        } else {
            queueLogger.warn { "Job '$jobId' has exceeded its retries count. Moving this job to the failed jobs queue." }
            queue.markAsFailedJob(payload, queueName, job.delayInSeconds)
            context.commit()
        }

        context.close()
    }

    override suspend fun process(container: Container) {
        job(container)
    }

    override fun rollback() {
        queueLogger.info { "Rolling back $this" }
        context.rollback()
    }
}
