package dev.alpas.queue.console

import dev.alpas.Container
import dev.alpas.queue.queueLogger
import java.io.File
import java.nio.channels.FileChannel
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

interface QueueCircuitBreaker {
    fun trip(container: Container)
}

interface QueueCircuitChecker : AutoCloseable {
    fun isTripped(): Boolean
    fun start(container: Container)
    override fun close() {
    }
}

class MemoryBasedQueueCircuitChecker(val path: String) : QueueCircuitChecker {
    //    val queueRestartTripPath = queueConfig.queueRestartTripPath
    private val channel by lazy {
        val restartFilePath = File(path).also { it.deleteOnExit() }
        FileChannel.open(
            restartFilePath.toPath(),
            StandardOpenOption.READ,
            StandardOpenOption.WRITE,
            StandardOpenOption.CREATE
        )
    }

    private val isClose get() = !channel.isOpen

    override fun isTripped(): Boolean {
        return isClose || channel.map(FileChannel.MapMode.READ_WRITE, 0, 4).asCharBuffer().get()
            .toInt() != 0
    }

    override fun start(container: Container) {
        val deleted = File(path).delete()
        queueLogger.info { "Deleted memory based queue tripper at $path? $deleted" }
    }

    override fun close() {
        if (channel.isOpen) {
            channel.close()
        }
    }
}

class MemoryBasedQueueCircuitBreaker(val path: String) : QueueCircuitBreaker {
    override fun trip(container: Container) {
        FileChannel.open(
            Paths.get(path),
            StandardOpenOption.READ,
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
