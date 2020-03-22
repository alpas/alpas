package dev.alpas.queue.database

import com.github.ajalt.clikt.output.TermUi
import dev.alpas.*
import dev.alpas.queue.ConnectionConfig
import dev.alpas.queue.Queue
import dev.alpas.queue.QueueConnection
import me.liuwj.ktorm.dsl.count
import java.sql.SQLSyntaxErrorException

@Suppress("unused")
open class DatabaseQueueConnection(env: Environment, config: ConnectionConfig? = null) : QueueConnection {
    open val defaultQueueName = config?.defaultQueueName ?: env("DB_DEFAULT_QUEUE_NAME", "default")
    override fun connect(container: Container): Queue {
        return DatabaseQueue(defaultQueueName, container.make())
    }

    override fun checkPrerequisites(): Boolean {
        return try {
            JobRecords.count()
            true
        } catch (exception: SQLSyntaxErrorException) {
            with(terminalColors) {
                deleteLastLine()
                exception.message?.let {
                    TermUi.echo(red(it))
                }
                TermUi.echo("Run ${yellow("alpas queue:tables")} followed by ${yellow("alpas migrate")} to add the required queue tables.")
                TermUi.echo(yellow("https://alpas.dev/docs/queues"))
            }
            false
        }
    }
}
