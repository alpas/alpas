package dev.alpas.queue.console

import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import dev.alpas.Container
import dev.alpas.console.Command
import dev.alpas.make
import dev.alpas.queue.Queue

class QueueWorkCommand(private val container: Container) :
    Command(name = "queue:work", help = "Start a queue worker.") {
    private val queueName by argument(help = "Name of the queue").optional()
    override fun run() {
        withColors {
            if (queueName == null) {
                echo(green("Running default queue..."))
            } else {
                echo(green("Running '$queueName' queue..."))
            }
        }
        val queue = container.make<Queue>()
        do {
            queue.dequeue(queueName)?.let {
                try {
                    it.job.invoke(container)
                    it.commit()
                } catch (ex: Exception) {
                    it.rollback(ex)
                }
            }
        } while (true)
    }
}
