package dev.alpas.tests

import dev.alpas.PackageClassLoader
import dev.alpas.routing.*

fun withRouter(block: Router.() -> Unit): Router {
    return Router() {
        block()
        compile(PackageClassLoader("dev.alpas.tests"))
    }
}

fun Route?.controllerMethod(): String? {
    return when (val handler = this?.handler) {
        is DynamicControllerHandler -> handler.method
        is ControllerHandler -> handler.method
        else -> null
    }
}

class TestController : Controller()
