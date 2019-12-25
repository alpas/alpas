package dev.alpas.queue.console

import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.default
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.unique
import com.github.ajalt.clikt.parameters.types.choice
import dev.alpas.Container
import dev.alpas.console.Command
import dev.alpas.make
import dev.alpas.queue.Queue
import dev.alpas.queue.QueueConfig

class QueueWorkCommand(private val container: Container) :
    Command(name = "queue:work", help = "Start a queue worker.") {
    private val queueConfig by lazy { container.make<QueueConfig>() }
    private val connection by argument(help = "The name of the connection to use.").choice(*queueConfig.registeredConnectionNames().toTypedArray()).default(
        queueConfig.defaultConnection
    )
    private val queues by option(help = "Name of the queues to process.").multiple().unique()
    override fun run() {
        showHint()
        val queue = queueConfig.connection(container, connection)
        do {
            if (queues.isEmpty()) {
                dequeue(container, queue)
            } else {
                dequeueMultiple(container, queue)
            }
        } while (true)
    }

    private fun showHint() {
        withColors {
            if (queues.isEmpty()) {
                echo(green("Processing default queue from '$connection' connection..."))
            } else {
                echo(green("Running '${queues.joinToString(", ")}' queue from '$connection' connection..."))
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
    private inline fun dequeueMultiple(container: Container, queue: Queue) {
        queues.forEach { name ->
            dequeue(container, queue, name)
        }
    }
}
