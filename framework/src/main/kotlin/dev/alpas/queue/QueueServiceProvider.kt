package dev.alpas.queue

import dev.alpas.Application
import dev.alpas.ServiceProvider
import dev.alpas.config
import dev.alpas.console.Command
import dev.alpas.make
import dev.alpas.queue.console.MakeJobCommand
import dev.alpas.queue.console.QueueWorkCommand

open class QueueServiceProvider : ServiceProvider {
    override fun register(app: Application) {
        app.singleton(Queue::class, app.config { QueueConfig(app.env) }.connection())
        app.bind(JobSerializer::class, JobSerializerImpl())
    }

    override fun stop(app: Application) {
        app.logger.debug { "Stopping $this" }
        app.make<Queue>().close()
    }

    override fun commands(app: Application): List<Command> {
        return listOf(QueueWorkCommand(app), MakeJobCommand(app.srcPackage))
    }
}
