package dev.alpas.routing

import dev.alpas.*
import dev.alpas.auth.Authenticatable
import dev.alpas.exceptions.httpExceptionFor
import dev.alpas.http.EventStreamResponse
import dev.alpas.http.HttpCall
import dev.alpas.http.RenderContext
import dev.alpas.http.RequestParamsBagContract
import dev.alpas.validation.SharedDataBag
import org.eclipse.jetty.http.HttpStatus
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

internal var dynamicControllerFactory: (() -> Controller)? = null

sealed class RouteHandler {
    abstract fun handle(call: HttpCall)
    protected fun invokeControllerMethod(methodName: String, controller: Controller, call: HttpCall) {
        fun Controller.invoke(methodName: String, call: HttpCall) {
            this.call = call
            this(call)
            if (!methodName.isBlank()) {
                val method = this.javaClass.getMethod(methodName, call.javaClass)
                method.invoke(this, call)
            }
        }

        // If a controller has specified some middleware for this method, let's pass the call
        // through all those middleware first and then pass the call to the actual handler.
        val controllerMiddleware = controller.middlewareFor(methodName, call).map { it.createInstance() }

        if (controllerMiddleware.isEmpty()) {
            controller.invoke(methodName, call)
        } else {
            Pipeline<HttpCall>().send(call).through(controllerMiddleware).then { controller.invoke(methodName, call) }
        }
    }
}

class ControllerHandler(val controller: KClass<out Controller>, val method: String) : RouteHandler() {
    override fun handle(call: HttpCall) {
        controllerInstance?.let { controller ->
            invokeControllerMethod(method, controller, call)
        }.orAbort()
    }

    override fun toString() = "${controller.qualifiedName}#$method"
    private val controllerInstance by lazy { controller.createInstance() as? Controller }
}

class DynamicControllerHandler(private val controllerName: String, val method: String) : RouteHandler() {
    var controller: Class<Controller>? = null
        private set

    private val controllerInstance by lazy {
        dynamicControllerFactory?.invoke()
            ?: controller?.kotlin?.createInstance()
            ?: throw IllegalArgumentException("Cannot create an instance of '$controllerName'.")
    }

    internal fun prepare(loader: PackageClassLoader) {
        val fullControllerName = "${loader.packageName}.$controllerName"
        @Suppress("UNCHECKED_CAST")
        controller = loader.classOfName(fullControllerName)?.loadClass() as? Class<Controller>
    }

    override fun handle(call: HttpCall) {
        invokeControllerMethod(method, controllerInstance, call).orAbort()
    }

    override fun toString() = "$controllerName#$method"
}

class ClosureHandler(private val closure: HttpCall.() -> Unit) : RouteHandler() {
    override fun handle(call: HttpCall) {
        closure(call)
    }

    override fun toString() = "Closure: $closure"
}

class SSEClosureHandler(private val closure: SSECall.() -> Unit) : RouteHandler() {
    override fun handle(call: HttpCall) {
        if (!call.isEventStream) {
            throw httpExceptionFor(HttpStatus.METHOD_NOT_ALLOWED_405)
        }

        EventStreamResponse().render(RenderContext(call, SharedDataBag()))
        call.startAsync()
        closure(SSECall(call))
    }

    override fun toString() = "SSE Closure: $closure"
}

class SSECall(private val call: HttpCall) : Container by call, RequestParamsBagContract by call {
    private val isClosed = AtomicBoolean(false)
    private var closeCallback: Runnable? = null

    val isAuthenticated = call.isAuthenticated
    val isFromGuest = !isAuthenticated
    val user: Authenticatable by lazy { call.user }

    fun onClose(closeCallback: Runnable) {
        this.closeCallback = closeCallback
    }

    fun notify(event: String = "message", data: Map<String, Any?>, id: Any? = null): Unit = synchronized(this) {
        try {
            val sb = StringBuilder()
            if (id != null) {
                sb.appendln("id: $id")
            }
            sb.appendln("event: $event")
            sb.appendln("data: ${data.toJson()}")
            sb.appendln()
            sb.appendln()
            call.servletRequest.asyncContext.response.apply {
                outputStream.print(sb.toString())
                flushBuffer()
            }
        } catch (e: IOException) {
            isClosed.set(true)
        } finally {
            if (isClosed.get()) {
                closeCallback?.run()
            }
        }
    }
}
