package dev.alpas.routing

import dev.alpas.PackageClassLoader
import dev.alpas.Pipeline
import dev.alpas.http.HttpCall
import dev.alpas.orAbort
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

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

class ControllerHandler(val controller: KClass<*>, val method: String) : RouteHandler() {
    override fun handle(call: HttpCall) {
        controllerInstance?.let { controller ->
            invokeControllerMethod(method, controller, call)
        }.orAbort()
    }

    override fun toString() = "$controller#$method"
    private val controllerInstance by lazy { controller.createInstance() as? Controller }
}

class DynamicControllerHandler(private val controllerName: String, val method: String) : RouteHandler() {

    var controller: Class<*>? = null
        private set

    private val controllerInstance by lazy { controller?.kotlin?.createInstance() as? Controller }

    internal fun prepare(loader: PackageClassLoader) {
        val fullControllerName = "${loader.packageName}.$controllerName"
        controller = loader.classOfName(fullControllerName)?.loadClass()
    }

    override fun handle(call: HttpCall) {
        controllerInstance?.let { controller ->
            invokeControllerMethod(method, controller, call)
        }.orAbort()
    }

    override fun toString() = "$controllerName#$method"
}

class ClosureHandler(private val closure: HttpCall.() -> Unit) : RouteHandler() {
    override fun handle(call: HttpCall) {
        closure(call)
    }

    override fun toString() = "Closure: $closure"
}
