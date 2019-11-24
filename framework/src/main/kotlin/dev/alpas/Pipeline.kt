package dev.alpas

typealias Handler<T> = (T) -> Unit

open class Middleware<T> : (T, Handler<T>) -> Unit {
    override fun invoke(call: T, forward: Handler<T>) {
    }
}

class Pipeline<T : Any> {
    lateinit var passable: T
    lateinit var pipes: Iterable<Middleware<T>>
    fun send(passable: T): Pipeline<T> {
        this.passable = passable
        return this
    }

    fun through(pipe: Middleware<T>, vararg pipes: Middleware<T>): Pipeline<T> {
        return through(pipes.toMutableList().apply { add(0, pipe) })
    }

    fun through(pipes: Iterable<Middleware<T>>): Pipeline<T> {
        this.pipes = pipes
        return this
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
        val pipeline = pipes.reversed().fold(prepareDestination(destination), carry())
        pipeline(passable)
    }
}
