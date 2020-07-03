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
import dev.alpas.queue.QueueConfig

class QueueWorkCommand(private val container: Container) : Command(name = "queue:work", help = "Start a queue worker") {
    private val queueConfig by lazy { container.make<QueueConfig>() }
    private val connection by argument(help = "The name of the connection to use.")
        .choice(*queueConfig.registeredConnectionNames().toTypedArray()).default(queueConfig.defaultConnection)
    private val queueName by option("--queue", help = "Comma separated queue names to process.")
    private val queueWorkers by option("--workers", help = "Number of queue workers.").int().default(2)
    private val sleep by option(help = "Seconds to sleep between if there are no new jobs.").int()

    override fun run() {
        printMessage()
        if (!queueConfig.checkPrerequisites(connection)) return
        QueueWorker(container, sleep).work(queueWorkers, queueName?.split(","), connection)
    }

    private fun printMessage() {
        withColors {
            if (queueName == null) {
                echo(green("Running default queue from '$connection' connection with $queueWorkers worker(s)..."))
            } else {
                echo(green("Running '$queueName' queue(s) from '$connection' connection with $queueWorkers worker(s)..."))
            }
        }
    }
}
