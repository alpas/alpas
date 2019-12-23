package dev.alpas.queue.jms

import dev.alpas.queue.Queue
import dev.alpas.queue.job.Job
import dev.alpas.queue.job.JobSerializer
import mu.KotlinLogging
import org.apache.qpid.jms.JmsConnectionFactory
import javax.jms.JMSContext
import javax.jms.JMSProducer

class JMSQueue(
    url: String,
    private val username: String,
    private val password: String,
    private val defaultQueueName: String,
    private val failedQueueName: String,
    private val serializer: JobSerializer
) : Queue {
    private val factory by lazy { JmsConnectionFactory(url) }
    private val logger by lazy { KotlinLogging.logger {} }

    override fun <T : Job> enqueue(job: T, onQueue: String?) {
        enqueue(job, onQueue, {})
    }

    override fun dequeue(from: String?, listener: (Job?) -> Unit) {
        factory.createContext(username, password, JMSContext.SESSION_TRANSACTED).use { context ->
            val queue = context.createQueue(from ?: defaultQueueName)
            try {
                val message = context.createConsumer(queue).receive()
                val deliveryCount = message.getIntProperty("JMSXDeliveryCount")
                val job = serializer.deserialize(message.getBody(String::class.java))
                val jobId = message.jmsMessageID
                if (deliveryCount > job.retries) {
                    logger.warn { "Job '$jobId' has exceeded its retries count. Moving this job to the '$failedQueueName' queue." }
                    enqueue(job, failedQueueName) { setProperty("JMS_AMQP_MA__AMQ_ORIG_ADDRESS", queue.queueName) }
                    context.commit()
                } else {
                    listener(job)
                    context.commit()
                    logger.info { "Job $jobId successfully processed!" }
                }
            } catch (exception: Throwable) {
                context.rollback()
                logger.error { exception.printStackTrace() }
            }
        }
    }

    private fun <T : Job> enqueue(job: T, onQueue: String?, setProperties: (JMSProducer.() -> Unit)) {
        factory.createContext(username, password).use { context ->
            val queue = context.createQueue(onQueue ?: defaultQueueName)
            context.createProducer().apply {
                setProperties()
                deliveryDelay = job.delayInSeconds * 1000
                send(queue, serializer.serialize(job))
            }
        }
    }
}
