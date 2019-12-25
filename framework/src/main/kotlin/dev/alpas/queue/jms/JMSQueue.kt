package dev.alpas.queue.jms

import dev.alpas.queue.JobHolder
import dev.alpas.queue.NoOpJobHolder
import dev.alpas.queue.Queue
import dev.alpas.queue.job.Job
import dev.alpas.queue.job.JobSerializer
import org.apache.qpid.jms.JmsConnectionFactory
import javax.jms.InvalidDestinationRuntimeException
import javax.jms.JMSContext
import javax.jms.JMSProducer

class JMSQueue(
    url: String,
    private val username: String,
    private val password: String,
    private val namespace: String,
    private val defaultQueueName: String,
    private val failedQueueName: String,
    private val serializer: JobSerializer
) : Queue {
    private val factory by lazy { JmsConnectionFactory(url) }

    override fun <T : Job> enqueue(job: T, onQueue: String?) {
        val queueName = queueName(onQueue)
        pushToQueue(serializer.serialize(job), queueName, job.delayInSeconds)
    }

    override fun dequeue(from: String?): JobHolder? {
        val queueName = queueName(from)
        val context = factory.createContext(username, password, JMSContext.SESSION_TRANSACTED)
        val queue = context.createQueue(queueName)
        return try {
            val message = context.createConsumer(queue).receive()
            val payload = message.getBody(String::class.java)
            val job = serializer.deserialize(payload)

            JMSJobHolder(job, context, message, queueName, payload, this)
        } catch (ex: InvalidDestinationRuntimeException) {
            NoOpJobHolder
        }
    }

    private fun queueName(name: String?): String {
        val queueName = name ?: defaultQueueName
        return "$namespace::$queueName"
    }

    private fun pushToQueue(
        payload: String,
        queueName: String,
        delayInSeconds: Long = 0,
        setProperties: (JMSProducer.() -> Unit) = {}
    ) {
        factory.createContext(username, password).use { context ->
            val queue = context.createQueue(queueName)
            context.createProducer().apply {
                setProperties()
                deliveryDelay = delayInSeconds * 1000
                send(queue, payload)
            }
        }
    }

    internal fun markAsFailedJob(payload: String, srcQueue: String, delayInSeconds: Long) {
        pushToQueue(payload, failedQueueName, delayInSeconds) { setProperty("JMS_AMQP_MA__AMQ_ORIG_ADDRESS", srcQueue) }
    }
}

