package dev.alpas.queue.console

import dev.alpas.Container
import dev.alpas.console.Command
import dev.alpas.make
import dev.alpas.queue.QueueConfig

class QueueRestartCommand(private val container: Container) : Command(name = "queue:restart", help = "Stop all queue workers") {
    override fun run() {
        withColors {
            echo(green("Stopping all queue workers..."))
        }
    }
}
