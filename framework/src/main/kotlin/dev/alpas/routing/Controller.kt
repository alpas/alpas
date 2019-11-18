package dev.alpas.routing

import dev.alpas.Middleware
import dev.alpas.auth.Authenticatable
import dev.alpas.http.HttpCall
import dev.alpas.make
import dev.alpas.notifications.Notifiable
import dev.alpas.notifications.Notification
import dev.alpas.notifications.NotificationDispatcher
import dev.alpas.queue.Job
import dev.alpas.queue.Queue
import kotlin.reflect.KClass

open class Controller {
    lateinit var call: HttpCall
    open operator fun invoke(call: HttpCall) {}

    fun middlewareFor(methodName: String, call: HttpCall): List<KClass<out Middleware<HttpCall>>> {
        return middleware(call)
            .filter { it.matches(methodName) }
            .flatMap { it.middleware.toList() }
    }

    open fun middleware(call: HttpCall): Iterable<ControllerMiddleware> {
        return listOf()
    }

    protected fun route(name: String, params: Map<String, Any>? = null, absolute: Boolean = true): String {
        return call.urlGenerator.route(name, params, absolute)
    }

    protected fun flash(name: String, payload: Any?) {
        call.session.flash(name, payload)
    }

    protected fun queue(job: Job, onQueue: String? = null) {
        call.make<Queue>().enqueue(job, onQueue, serializer = call.make())
    }

    protected fun <T : Notifiable> send(notification: Notification<T>, notifiable: T, vararg notifiables: T) {
        call.make<NotificationDispatcher>().dispatch(notification, notifiable, *notifiables)
    }

    protected fun <T : Notifiable> send(notification: Notification<T>, notifiables: List<T>) {
        call.make<NotificationDispatcher>().dispatch(notification, notifiables)
    }

    protected fun <T : Notifiable> send(notification: Notification<T>, notifiable: T) {
        call.make<NotificationDispatcher>().dispatch(notification, notifiable)
    }

    protected fun <T : Authenticatable> caller(): T = call.caller()
}

class ControllerMiddleware(val method: String? = null, vararg val middleware: KClass<out Middleware<HttpCall>>) {
    constructor(vararg middleware: KClass<out Middleware<HttpCall>>) : this(method = null, middleware = *middleware)

    fun matches(method: String): Boolean {
        return this.method == null || this.method == method
    }
}
