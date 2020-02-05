package dev.alpas

// A handler for forwarding an instance of T
typealias Handler<T> = (T) -> Unit

open class Middleware<T> : (T, Handler<T>) -> Unit {
    override fun invoke(passable: T, forward: Handler<T>) {
    }
}

class Pipeline<T : Any> {
    private lateinit var passable: T
    private lateinit var pipes: Iterable<Middleware<T>>
    private var isInvoked = false

    fun send(passable: T): Pipeline<T> {
        checkIfInvoked()
        this.passable = passable
        return this
    }

    private fun checkIfInvoked() {
        if (isInvoked) {
            throw IllegalStateException("Pipeline is already invoked!")
        }
    }

    fun through(pipe: Middleware<T>, vararg pipes: Middleware<T>): Pipeline<T> {
        checkIfInvoked()
        return through(listOf(pipe) + pipes)
    }

    fun through(pipes: Iterable<Middleware<T>>) = apply {
        checkIfInvoked()
        if (this::pipes.isInitialized) {
            this.pipes = this.pipes + pipes
        } else {
            this.pipes = pipes
        }
    }

    private fun prepareDestination(destination: Handler<T>): (T) -> Unit {
        return fun(passable: T) {
            return destination(passable)
        }
    }

    private fun carry(): (Handler<T>, Middleware<T>) -> Handler<T> {
        return fun(stack, pipe): Handler<T> {
            return fun(passable) = pipe(passable, stack)
        }
    }

    fun then(destination: Handler<T>) {
        checkIfInvoked()
        val pipeline = pipes.reversed().fold(prepareDestination(destination), carry())
        pipeline(passable)
        isInvoked = true
    }
}
