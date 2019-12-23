package dev.alpas.queue

import mu.KotlinLogging
import org.apache.qpid.jms.JmsConnectionFactory
import javax.jms.JMSContext
import javax.jms.JMSProducer

class JMSQueue(
    url: String,
    private val username: String,
    private val password: String,
    private val defaultQueueName: String,
    private val failedQueueName: String
) : Queue {
    private val factory by lazy { JmsConnectionFactory(url) }
    private val logger by lazy { KotlinLogging.logger {} }

    override fun <T : Job> enqueue(job: T, onQueue: String?, serializer: JobSerializer) {
        enqueue(job, onQueue, serializer, {})
    }

    override fun dequeue(from: String?, serializer: JobSerializer, listener: (Job?) -> Unit) {
        factory.createContext(username, password, JMSContext.SESSION_TRANSACTED).use { context ->
            val queue = context.createQueue(from ?: defaultQueueName)
            try {
                val message = context.createConsumer(queue).receive()
                val deliveryCount = message.getIntProperty("JMSXDeliveryCount")
                val job = serializer.deserialize(message.getBody(String::class.java))
                if (deliveryCount > job.retries) {
                    logger.warn { "Job '$job' has exceeded its retries count. Putting this job in the '$failedQueueName' queue." }
                    // move this job to the failed queue
                    enqueue(job, failedQueueName, serializer = serializer) {
                        setProperty("JMS_AMQP_MA__AMQ_ORIG_ADDRESS", queue.queueName)
                    }
                    context.commit()
                } else {
                    listener(job)
                    context.commit()
                }
            } catch (exception: Throwable) {
                context.rollback()
                logger.error { exception.printStackTrace() }
            }
        }
    }

    private fun <T : Job> enqueue(
        job: T,
        onQueue: String?,
        serializer: JobSerializer,
        setProperties: (JMSProducer.() -> Unit)
    ) {
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
