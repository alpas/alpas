package dev.alpas.queue.console

import java.io.File
import java.nio.channels.FileChannel
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

class CircuitBreaker(val path: String) : AutoCloseable {
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

    fun isTripped(): Boolean {
        return isClose || channel.map(FileChannel.MapMode.READ_WRITE, 0, 4).asCharBuffer().get()
            .toInt() != 0
    }

    companion object {
        fun trip(path: String) {
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

    override fun close() {
        if (channel.isOpen) {
            channel.close()
        }
    }
}
