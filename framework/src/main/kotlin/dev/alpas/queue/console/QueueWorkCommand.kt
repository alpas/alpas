package dev.alpas.queue.console

import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import dev.alpas.Container
import dev.alpas.console.Command
import dev.alpas.make
import dev.alpas.queue.Queue
import dev.alpas.queue.QueueConfig

class QueueWorkCommand(private val container: Container) :
    Command(name = "queue:work", help = "Start a queue worker.") {
    private val queueConfig by lazy { container.make<QueueConfig>() }
    private val connection by argument(help = "The name of the connection to use.")
        .choice(*queueConfig.registeredConnectionNames().toTypedArray()).default(queueConfig.defaultConnection)
    private val queueName by option("--queue", help = "Name of the queue to process.")
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
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun dequeue(container: Container, queue: Queue, name: String? = null) {
        queue.dequeue(name)?.let {
            try {
                it.process(container)
                it.commit()
            } catch (ex: Exception) {
                it.rollback(ex)
            }
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun dequeueMultiple(container: Container, queue: Queue, names: List<String>) {
        names.forEach { name ->
            dequeue(container, queue, name)
        }
    }
}
