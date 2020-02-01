package dev.alpas.routing

import dev.alpas.Middleware
import dev.alpas.http.HttpCall
import dev.alpas.http.Method
import kotlin.reflect.KClass
import kotlin.reflect.KFunction2

abstract class RoutableBase(
    private val middleware: Set<KClass<out Middleware<HttpCall>>>,
    private val middlewareGroups: Set<String>
) {
    internal val routes = mutableListOf<Route>()

    // closures

    fun get(closure: HttpCall.() -> Unit) = get("", middleware, closure)
    fun get(path: String, closure: HttpCall.() -> Unit) = get(path, middleware, closure)
    fun get(path: String, middleware: Set<KClass<out Middleware<HttpCall>>>, closure: HttpCall.() -> Unit) =
        add(Method.GET, path, ClosureHandler(closure), middleware)

    fun post(closure: HttpCall.() -> Unit) = post("", middleware, closure)
    fun post(path: String, closure: HttpCall.() -> Unit) = post(path, middleware, closure)
    fun post(path: String, middleware: Set<KClass<out Middleware<HttpCall>>>, closure: HttpCall.() -> Unit) =
        add(Method.POST, path, ClosureHandler(closure), middleware)

    fun delete(closure: HttpCall.() -> Unit) = delete("", middleware, closure)
    fun delete(path: String, closure: HttpCall.() -> Unit) = delete(path, middleware, closure)
    fun delete(path: String, middleware: Set<KClass<out Middleware<HttpCall>>>, closure: HttpCall.() -> Unit) =
        add(Method.DELETE, path, ClosureHandler(closure), middleware)

    fun patch(closure: HttpCall.() -> Unit) = patch("", middleware, closure)
    fun patch(path: String, closure: HttpCall.() -> Unit) = patch(path, middleware, closure)
    fun patch(path: String, middleware: Set<KClass<out Middleware<HttpCall>>>, closure: HttpCall.() -> Unit) =
        add(Method.PATCH, path, ClosureHandler(closure), middleware)

    // dynamic controllers

    internal fun get(path: String, controller: String, method: String = "index") =
        get(path, middleware, controller, method)

    internal fun get(
        path: String,
        middleware: Set<KClass<out Middleware<HttpCall>>>,
        controller: String,
        method: String
    ) = add(Method.GET, path, DynamicControllerHandler(controller, method), middleware)

    internal fun post(path: String, controller: String, method: String) =
        post(path, middleware, controller, method)

    internal fun post(
        path: String,
        middleware: Set<KClass<out Middleware<HttpCall>>>,
        controller: String,
        method: String = "store"
    ) = add(Method.POST, path, DynamicControllerHandler(controller, method), middleware)

    internal fun delete(path: String, controller: String, method: String) =
        delete(path, middleware, controller, method)

    internal fun delete(
        path: String,
        middleware: Set<KClass<out Middleware<HttpCall>>>,
        controller: String,
        method: String = "delete"
    ) = add(Method.DELETE, path, DynamicControllerHandler(controller, method), middleware)

    internal fun patch(path: String, controller: String, method: String) =
        patch(path, middleware, controller, method)

    internal fun patch(
        path: String,
        middleware: Set<KClass<out Middleware<HttpCall>>>,
        controller: String,
        method: String = "update"
    ) = add(Method.PATCH, path, DynamicControllerHandler(controller, method), middleware)

    // controller kclasses

    // get<HomeController>("home") or get<HomeController>("home", "all")
    inline fun <reified T : Controller> get(path: String, method: String = "index") =
        get(path, T::class, method)

    fun get(controller: KClass<out Controller>, method: String = "index") = get("", controller, method)

    // get("home", HomeController::all)
    inline fun <reified T : Controller> get(method: KFunction2<T, HttpCall, Unit>) =
        get("", T::class, method.name)

    inline fun <reified T : Controller> get(method: String = "index") =
        get("", T::class, method)

    fun <T : Controller> get(controller: KClass<T>, method: KFunction2<T, HttpCall, Unit>) =
        get("", controller, method.name)

    // get("home", HomeController::all)
    inline fun <reified T : Controller> get(path: String, method: KFunction2<T, HttpCall, Unit>) =
        get(path, T::class, method.name)

    fun <T : Controller> get(path: String, controller: KClass<T>, method: KFunction2<T, HttpCall, Unit>) =
        add(Method.GET, path, ControllerHandler(controller, method.name), middleware)

    fun get(path: String, controller: KClass<out Controller>, method: String = "index") =
        add(Method.GET, path, ControllerHandler(controller, method), middleware)


    inline fun <reified T : Controller> post(path: String, method: String = "store") =
        post(path, T::class, method)

    fun post(controller: KClass<out Controller>, method: String = "store") = post("", controller, method)

    inline fun <reified T : Controller> post(method: KFunction2<T, HttpCall, Unit>) =
        post("", T::class, method.name)

    inline fun <reified T : Controller> post(method: String = "store") =
        post("", T::class, method)

    fun <T : Controller> post(controller: KClass<T>, method: KFunction2<T, HttpCall, Unit>) =
        post("", controller, method.name)

    inline fun <reified T : Controller> post(path: String, method: KFunction2<T, HttpCall, Unit>) =
        post(path, T::class, method.name)

    fun <T : Controller> post(path: String, controller: KClass<T>, method: KFunction2<T, HttpCall, Unit>) =
        add(Method.POST, path, ControllerHandler(controller, method.name), middleware)

    fun post(path: String, controller: KClass<out Controller>, method: String = "store") =
        add(Method.POST, path, ControllerHandler(controller, method), middleware)


    inline fun <reified T : Controller> delete(path: String, method: String = "delete") =
        delete(path, T::class, method)

    fun delete(controller: KClass<out Controller>, method: String = "delete") =
        delete("", controller, method)

    inline fun <reified T : Controller> delete(method: KFunction2<T, HttpCall, Unit>) =
        delete("", T::class, method.name)

    inline fun <reified T : Controller> delete(method: String = "delete") =
        delete("", T::class, method)

    fun <T : Controller> delete(controller: KClass<T>, method: KFunction2<T, HttpCall, Unit>) =
        delete("", controller, method.name)

    inline fun <reified T : Controller> delete(path: String, method: KFunction2<T, HttpCall, Unit>) =
        delete(path, T::class, method.name)

    fun delete(path: String, controller: KClass<out Controller>, method: String = "delete") =
        add(Method.DELETE, path, ControllerHandler(controller, method), middleware)

    fun <T : Controller> delete(path: String, controller: KClass<T>, method: KFunction2<T, HttpCall, Unit>) =
        add(Method.DELETE, path, ControllerHandler(controller, method.name), middleware)


    inline fun <reified T : Controller> patch(path: String, method: String = "update") =
        patch(path, T::class, method)

    fun patch(controller: KClass<out Controller>, method: String = "update") =
        patch("", controller, method)

    inline fun <reified T : Controller> patch(method: KFunction2<T, HttpCall, Unit>) =
        patch("", T::class, method.name)

    inline fun <reified T : Controller> patch(method: String = "update"): Route {
        return patch("", T::class, method)
    }

    fun <T : Controller> patch(controller: KClass<T>, method: KFunction2<T, HttpCall, Unit>) =
        patch("", controller, method.name)

    inline fun <reified T : Controller> patch(path: String, method: KFunction2<T, HttpCall, Unit>) =
        patch(path, T::class, method.name)

    fun patch(path: String, controller: KClass<out Controller>, method: String = "update") =
        add(Method.PATCH, path, ControllerHandler(controller, method), middleware)

    fun <T : Controller> patch(path: String, controller: KClass<T>, method: KFunction2<T, HttpCall, Unit>) =
        add(Method.PATCH, path, ControllerHandler(controller, method.name), middleware)

    fun group(
        prefix: String,
        middleware: Set<KClass<out Middleware<HttpCall>>>,
        block: RouteGroup.() -> Unit
    ): RouteGroup {
        val childGroupPrefix = fullPath(prefix)
        val childGroup = RouteGroup(childGroupPrefix, middleware.toMutableSet()).apply {
            block()
            // The name of child group could have been set while calling the block above. So, we need to reassign
            // the group name for all the routes here. Same goes for child group's middleware as well.
            routes.forEach { route ->
                route.middleware(middleware)
                route.groupName = fullName(route.groupName)
            }
        }
        routes.addAll(childGroup.routes)
        return childGroup
    }

    fun group(middleware: Set<KClass<out Middleware<HttpCall>>> = mutableSetOf(), block: RouteGroup.() -> Unit) =
        group("", middleware, block)

    fun group(
        middleware: KClass<out Middleware<HttpCall>>,
        vararg others: KClass<out Middleware<HttpCall>>,
        block: RouteGroup.() -> Unit
    ) =
        group("", setOf(middleware, *others), block)

    fun group(block: RouteGroup.() -> Unit) = group("", mutableSetOf(), block)
    fun group(prefix: String, block: RouteGroup.() -> Unit) = group(prefix, mutableSetOf(), block)

    protected open fun add(
        method: Method,
        path: String,
        handler: RouteHandler,
        middleware: Set<KClass<out Middleware<HttpCall>>>
    ): Route {
        return Route(method, fullPath(path), middleware.toMutableSet(), middlewareGroups.toMutableSet(), handler).also {
            routes.add(it)
        }
    }

    protected open fun fullName(name: String) = name
    protected open fun fullPath(path: String) = path.ifBlank { "/" }
}
