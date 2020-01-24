package dev.alpas.queue.console

import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.int
import dev.alpas.Container
import dev.alpas.console.Command
import dev.alpas.make
import dev.alpas.queue.Queue
import dev.alpas.queue.QueueConfig
import java.time.Duration

class QueueWorkCommand(private val container: Container) :
    Command(name = "queue:work", help = "Start a queue worker") {
    private val queueConfig by lazy { container.make<QueueConfig>() }
    private val connection by argument(help = "The name of the connection to use.")
        .choice(*queueConfig.registeredConnectionNames().toTypedArray()).default(queueConfig.defaultConnection)
    private val queueName by option("--queue", help = "Name of the queue to process.")
    private val sleep by option(help = "Seconds to sleep between if there are no new jobs.").int()
    private val sleepDuration by lazy { sleep?.let { Duration.ofSeconds(it.toLong()) } }

    override fun run() {
        printMessage()
        val queue = queueConfig.connection(container, connection)
        val queueNames = queueName?.split(",") ?: emptyList()
        do {
            if (queueName == null) {
                dequeue(container, queue)
            } else {
                dequeueMultiple(container, queue, queueNames)
            }
        } while (true)
    }

    private fun printMessage() {
        withColors {
            if (queueName == null) {
                echo(green("Processing default queue from '$connection' connection..."))
            } else {
                echo(green("Running '$queueName' queue(s) from $connection' connection..."))
            }
            echo(yellow("https://alpas.dev/docs/queues"))
        }
    }

    private fun dequeue(container: Container, queue: Queue, name: String? = null) {
        queue.dequeue(name, sleepDuration)?.let {
            try {
                it.process(container)
                it.commit()
                // Since we received a non-null JobHolder, we'll continue to dequeue from
                // the same queue. When we get a null we'll yield to the next queue.
                this.dequeue(container, queue, name)
            } catch (ex: Exception) {
                it.rollback(ex)
            }
        }
    }

    private fun dequeueMultiple(container: Container, queue: Queue, names: List<String>) {
        names.forEach { name ->
            dequeue(container, queue, name)
        }
    }
}
