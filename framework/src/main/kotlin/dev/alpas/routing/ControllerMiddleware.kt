package dev.alpas.routing

import dev.alpas.Middleware
import dev.alpas.http.HttpCall
import kotlin.reflect.KClass

class ControllerMiddleware(
    val methods: List<String>? = null,
    private val middleware: KClass<out Middleware<HttpCall>>,
    private vararg val others: KClass<out Middleware<HttpCall>>
) {
    val all by lazy { listOf(middleware, *others) }

    constructor(
        method: String,
        middleware: KClass<out Middleware<HttpCall>>,
        vararg others: KClass<out Middleware<HttpCall>>
    ) : this(
        methods = listOf(method),
        middleware = middleware,
        others = *others
    )

    constructor(middleware: KClass<out Middleware<HttpCall>>, vararg others: KClass<out Middleware<HttpCall>>) : this(
        methods = null,
        middleware = middleware,
        others = *others
    )

    fun matches(method: String): Boolean {
        return this.methods == null || this.methods.contains(method)
    }
}
