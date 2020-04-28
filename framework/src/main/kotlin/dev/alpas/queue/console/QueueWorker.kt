package dev.alpas.queue.console

import dev.alpas.Container
import dev.alpas.make
import dev.alpas.queue.Queue
import dev.alpas.queue.QueueConfig
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import java.io.File
import java.time.Duration

class QueueWorker(private val container: Container, sleep: Int?) {
    private val logger = KotlinLogging.logger {}
    private val queueConfig = container.make<QueueConfig>()
    private val sleepDuration = sleep?.let { Duration.ofSeconds(it.toLong()) }
    private val circuitBreaker = CircuitBreaker(queueConfig.queueRestartTripPath)

    internal fun work(noOfWorkers: Int, queueNames: List<String>?, connection: String) = runBlocking {
        // Since we are just starting, we don't care about the restart trigger of the past
        File(queueConfig.queueRestartTripPath).delete()
        (1..noOfWorkers).map {
            launch {
                val queue = queueConfig.connection(container, connection)
                do {
                    if (shouldCancelJob()) {
                        coroutineContext.cancel()
                        logger.warn { "Queue cancel request received. Exiting..." }
                        cancel()
                        break
                    }
                    if (queueNames.isNullOrEmpty()) {
                        dequeue(queue)
                    } else {
                        dequeueMultiple(queue, queueNames)
                    }
                } while (true)
            }
        }
    }

    private suspend fun dequeue(queue: Queue, name: String? = null) {
        queue.dequeue(name, sleepDuration)?.let { job ->
            if (shouldCancelJob()) {
                logger.debug { "Queue cancel request received. Won't process the job $job on queue $name" }
                close()
                job.rollback()
            } else {
                try {
                    job.process(container)
                    job.commit()
                    // Since we received a non-null JobHolder, we'll continue to dequeue from
                    // the same queue. When we get a null we'll yield to the next queue.
                    logger.debug { "Completed processing job $job on from queue $name. Continuing to process another job on $name..." }
                    this.dequeue(queue, name)
                } catch (ex: Exception) {
                    job.rollback(ex)
                }
            }
        }
    }

    private fun shouldCancelJob(): Boolean {
        return circuitBreaker.isTripped()
    }

    private suspend fun dequeueMultiple(queue: Queue, names: List<String>) {
        for (name in names) {
            if (shouldCancelJob()) {
                logger.debug { "Queue cancel request received. Won't process queue $name" }
                close()
                break
            } else {
                dequeue(queue, name)
                logger.debug { "Completed processing job from queue $name. Yielding to the next queue..." }
            }
        }
    }

    private fun close() {
        circuitBreaker.close()
    }
}
