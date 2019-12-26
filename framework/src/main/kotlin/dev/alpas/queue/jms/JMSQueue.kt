package dev.alpas.queue.jms

import dev.alpas.queue.JobHolder
import dev.alpas.queue.Queue
import dev.alpas.queue.job.Job
import dev.alpas.queue.job.JobSerializer
import org.apache.qpid.jms.JmsConnectionFactory
import java.time.Duration
import javax.jms.InvalidDestinationRuntimeException
import javax.jms.JMSContext
import javax.jms.JMSProducer

open class JMSQueue(
    url: String,
    private val username: String,
    private val password: String,
    private val namespace: String,
    private val defaultQueueName: String,
    private val failedQueueName: String,
    private val serializer: JobSerializer,
    private val timeout: Duration = Duration.ofSeconds(30)
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
            val message = context.createConsumer(queue).receive(timeout.toMillis())
            val payload = message?.getBody(String::class.java)

            if (payload == null || isMissingQueueDummyPayload(payload, queueName)) {
                // Skip processing this short-lived dummy job
                context.commit()
                context.close()
                null
            } else {
                val job = serializer.deserialize(payload)
                JMSJobHolder(job, context, message, queueName, payload, this)
            }
        } catch (ex: InvalidDestinationRuntimeException) {
            context.commit()
            context.close()
            // So that we don't get into this infinite loop and we certainly don't want to throw
            // an exception crashing queue:work command and forcing users to restart the queue.
            createMissingQueue(queueName)
            null
        }
    }

    private fun isMissingQueueDummyPayload(payload: String?, queueName: String): Boolean {
        return makeMissingQueueDummyPayload(queueName) == payload
    }

    private fun makeMissingQueueDummyPayload(queueName: String): String {
        return "--$queueName--"
    }

    private fun createMissingQueue(queueName: String) {
        factory.createContext(username, password, JMSContext.SESSION_TRANSACTED).use { context ->
            val queue = context.createQueue(queueName)
            context.createProducer().send(queue, makeMissingQueueDummyPayload(queueName))
            context.commit()
        }
    }

    private fun queueName(name: String?): String {
        val queueName = name ?: defaultQueueName
        return "$namespace::$queueName"
    }

    protected open fun pushToQueue(
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

    open fun markAsFailedJob(payload: String, srcQueue: String, delayInSeconds: Long) {
        pushToQueue(payload, failedQueueName, delayInSeconds) { setProperty("JMS_AMQP_MA__AMQ_ORIG_ADDRESS", srcQueue) }
    }
}
