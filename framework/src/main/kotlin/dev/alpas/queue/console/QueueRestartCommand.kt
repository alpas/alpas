package dev.alpas.queue.console

import dev.alpas.Container
import dev.alpas.console.Command
import dev.alpas.make
import dev.alpas.queue.QueueConfig
import java.io.File
import java.nio.channels.FileChannel
import java.nio.file.StandardOpenOption

class QueueRestartCommand(private val container: Container) : Command(name = "queue:restart", help = "Stop all queue workers") {
    private val queueConfig by lazy { container.make<QueueConfig>() }
    private val restartFilePath = File(queueConfig.restartTriggerFilename)

    override fun run() {
        withColors {
            echo(green("Stopping all queue workers..."))
        }
        FileChannel.open(
            restartFilePath.toPath(),
            StandardOpenOption.WRITE,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        ).use { channel ->
            val string = "${1}\u0000".toCharArray()
            val buff = channel.map(FileChannel.MapMode.READ_WRITE, 0, 4)
            buff.asCharBuffer().put(string)
        }
    }
}
