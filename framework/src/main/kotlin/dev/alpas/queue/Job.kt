package dev.alpas.queue

import dev.alpas.Container

open class Job {
    open val delayInSeconds: Long = 1
    open val retries: Int = 3
    open operator fun invoke(container: Container) {}
}
