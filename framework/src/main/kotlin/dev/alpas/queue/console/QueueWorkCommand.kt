package dev.alpas.queue.console

import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.default
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.int
import dev.alpas.Container
import dev.alpas.console.Command
import dev.alpas.make
import dev.alpas.queue.Queue
import dev.alpas.queue.QueueConfig
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.Duration

class QueueWorkCommand(private val container: Container) :
    Command(name = "queue:work", help = "Start a queue worker") {
    private val queueConfig by lazy { container.make<QueueConfig>() }
    private val connection by argument(help = "The name of the connection to use.")
        .choice(*queueConfig.registeredConnectionNames().toTypedArray()).default(queueConfig.defaultConnection)
    private val queueName by option("--queue", help = "Comma separated queue names to process.")
    private val queueWorkers by option("--workers", help = "Number of queue workers.").int().default(2)
    private val sleep by option(help = "Seconds to sleep between if there are no new jobs.").int()
    private val sleepDuration by lazy { sleep?.let { Duration.ofSeconds(it.toLong()) } }

    override fun run() = runBlocking {
        printMessage()

        if (!queueConfig.checkPrerequisites(connection)) {
            return@runBlocking
        }
        val queueNames = queueName?.split(",") ?: emptyList()
        (1..queueWorkers).map {
            launch {
                val queue = queueConfig.connection(container, connection)
                do {
                    if (queueName == null) {
                        dequeue(container, queue)
                    } else {
                        dequeueMultiple(container, queue, queueNames)
                    }
                } while (true)
            }
        }
    }

    private fun printMessage() {
        withColors {
            if (queueName == null) {
                echo(green("Processing default queue from '$connection' connection..."))
            } else {
                echo(green("Running '$queueName' queue(s) from $connection' connection..."))
            }
        }
    }

    private suspend fun dequeue(container: Container, queue: Queue, name: String? = null) {
        queue.dequeue(name, sleepDuration)?.let { job ->
            try {
                job.process(container)
                job.commit()
                // Since we received a non-null JobHolder, we'll continue to dequeue from
                // the same queue. When we get a null we'll yield to the next queue.
                this.dequeue(container, queue, name)
            } catch (ex: Exception) {
                job.rollback(ex)
            }
        }
    }

    private suspend fun dequeueMultiple(container: Container, queue: Queue, names: List<String>) {
        names.forEach { name ->
            dequeue(container, queue, name)
        }
    }
}
