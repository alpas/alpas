package dev.alpas.queue.job

import dev.alpas.Container

open class Job {
    open val delayInSeconds: Long = 1
    open val tries: Int = 3
    open operator fun invoke(container: Container) {}
}
