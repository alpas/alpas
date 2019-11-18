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
    val logger by lazy { KotlinLogging.logger {} }

    override fun <T : Job> enqueue(
        job: T,
        onQueue: String?,
        serializer: JobSerializer,
        setProperties: (JMSProducer.() -> Unit)
    ) {
        factory.createContext(username, password).use {
            val queue = it.createQueue(onQueue ?: defaultQueueName)
            it.createProducer().also(setProperties).setDeliveryDelay(job.delayInSeconds * 1000)
                .send(queue, serializer.serialize(job))
        }
    }

    override fun dequeue(from: String?, serializer: JobSerializer, listener: (Job?) -> Unit) {
        factory.createContext(username, password, JMSContext.SESSION_TRANSACTED).use {
            val queue = it.createQueue(from ?: defaultQueueName)
            try {
                val message = it.createConsumer(queue).receive()
                val deliveryCount = message.getIntProperty("JMSXDeliveryCount")
                val job = serializer.deserialize(message.getBody(String::class.java))
                if (deliveryCount > job.retries) {
                    logger.warn { "Job '$job' has exceeded its retries count. Putting this job in the '$failedQueueName' queue." }
                    // move this job to failed queue
                    enqueue(job, failedQueueName, serializer = serializer) {
                        setProperty("JMS_AMQP_MA__AMQ_ORIG_ADDRESS", queue.queueName)
                    }
                    it.commit()
                } else {
                    listener(job)
                    it.commit()
                }
            } catch (exception: Throwable) {
                it.rollback()
                logger.error { exception.printStackTrace() }
            }
        }
    }
}
