package dev.alpas.tests

import dev.alpas.Handler
import dev.alpas.Middleware
import dev.alpas.Pipeline
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PipelineTest {
    @Test
    fun `a passable object is sent through in order`() {
        val passables = mutableListOf<String>()
        val middleware1 = object : Middleware<String>() {
            override fun invoke(call: String, forward: Handler<String>) {
                passables.add(call)
                forward("$call 1")
            }
        }
        val middleware2 = object : Middleware<String>() {
            override fun invoke(call: String, forward: Handler<String>) {
                passables.add(call)
                forward("$call 2")
            }
        }

        Pipeline<String>().send("start").through(middleware2, middleware1).then {
            assertEquals("start 2 1", it)
        }

        assertEquals("start", passables[0])
        assertEquals("start 2", passables[1])
    }

    @Test
    fun `pipeline is activated only after invoking then()`() {
        val passables = mutableListOf<String>()
        val middleware1 = object : Middleware<String>() {
            override fun invoke(call: String, forward: Handler<String>) {
                passables.add(call)
                forward("$call 1")
            }
        }
        val middleware2 = object : Middleware<String>() {
            override fun invoke(call: String, forward: Handler<String>) {
                passables.add(call)
                forward("$call 2")
            }
        }
        Pipeline<String>().send("start").through(middleware2, middleware1)
        assertTrue(passables.isEmpty())
    }

    @Test
    fun `more pipes can be appended to a pipeline`() {
        val passables = mutableListOf<String>()
        val middleware1 = object : Middleware<String>() {
            override fun invoke(call: String, forward: Handler<String>) {
                passables.add(call)
                forward("$call 1")
            }
        }
        val middleware2 = object : Middleware<String>() {
            override fun invoke(call: String, forward: Handler<String>) {
                passables.add(call)
                forward("$call 2")
            }
        }
        val pipeline = Pipeline<String>().send("start").through(middleware2, middleware1)
        val middleware3 = object : Middleware<String>() {
            override fun invoke(call: String, forward: Handler<String>) {
                passables.add(call)
                forward("$call 3")
            }
        }

        pipeline.through(middleware3)

        pipeline.then {
            assertEquals("start 2 1 3", it)
        }

        assertEquals("start", passables[0])
        assertEquals("start 2", passables[1])
        assertEquals("start 2 1", passables[2])
    }

    @Test
    fun `an invoked pipeline cannot be invoked again`() {
        val middleware = object : Middleware<String>() {
            override fun invoke(call: String, forward: Handler<String>) {
                forward("$call 2")
            }
        }
        val pipeline = Pipeline<String>()
        pipeline.send("start").through(middleware).then { }

        assertThrows(IllegalStateException::class.java) {
            pipeline.send("test")
        }

        assertThrows(IllegalStateException::class.java) {
            pipeline.through(object : Middleware<String>() {})
        }

        assertThrows(IllegalStateException::class.java) {
            pipeline.then {}
        }
    }
}
