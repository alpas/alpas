package dev.alpas.routing

import dev.alpas.Middleware
import dev.alpas.extensions.toKebabCase
import dev.alpas.http.HttpCall
import dev.alpas.http.Method
import kotlin.reflect.KClass
import kotlin.reflect.KFunction2

const val DEFAULT_GET_METHOD = "index"
const val DEFAULT_POST_METHOD = "store"
const val DEFAULT_DELETE_METHOD = "delete"
const val DEFAULT_PATCH_METHOD = "update"

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

    fun sse(closure: SSECall.() -> Unit) = sse("", middleware, closure)
    fun sse(path: String, closure: SSECall.() -> Unit) = sse(path, middleware, closure)
    fun sse(path: String, middleware: Set<KClass<out Middleware<HttpCall>>>, closure: SSECall.() -> Unit) =
        add(Method.GET, path, SSEClosureHandler(closure), middleware)

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

    internal fun get(path: String, controller: String, method: String = DEFAULT_GET_METHOD) =
        get(path, middleware, controller, method)

    internal fun get(
        path: String,
        middleware: Set<KClass<out Middleware<HttpCall>>>,
        controller: String,
        method: String
    ) = add(Method.GET, path, DynamicControllerHandler(controller, method), middleware).name(method)

    internal fun post(path: String, controller: String, method: String) =
        post(path, middleware, controller, method)

    internal fun post(
        path: String,
        middleware: Set<KClass<out Middleware<HttpCall>>>,
        controller: String,
        method: String = DEFAULT_POST_METHOD
    ) = add(Method.POST, path, DynamicControllerHandler(controller, method), middleware).name(method)

    internal fun delete(path: String, controller: String, method: String) =
        delete(path, middleware, controller, method)

    internal fun delete(
        path: String,
        middleware: Set<KClass<out Middleware<HttpCall>>>,
        controller: String,
        method: String = DEFAULT_DELETE_METHOD
    ) = add(Method.DELETE, path, DynamicControllerHandler(controller, method), middleware).name(method)

    internal fun patch(path: String, controller: String, method: String) =
        patch(path, middleware, controller, method)

    internal fun patch(
        path: String,
        middleware: Set<KClass<out Middleware<HttpCall>>>,
        controller: String,
        method: String = DEFAULT_PATCH_METHOD
    ) = add(Method.PATCH, path, DynamicControllerHandler(controller, method), middleware).name(method)

    // controller kclasses

    // get<HomeController>("home") or get<HomeController>("home", "all")
    inline fun <reified T : Controller> get(path: String = "", method: String = DEFAULT_GET_METHOD) =
        get(path, T::class, method)

    fun get(controller: KClass<out Controller>, method: String = DEFAULT_GET_METHOD) = get("", controller, method)

    // get("home", HomeController::all)
    inline fun <reified T : Controller> get(method: KFunction2<T, HttpCall, Unit>) =
        get("", T::class, method.name)

    fun <T : Controller> get(controller: KClass<T>, method: KFunction2<T, HttpCall, Unit>) =
        get("", controller, method.name)

    // get("home", HomeController::all)
    inline fun <reified T : Controller> get(path: String, method: KFunction2<T, HttpCall, Unit>) =
        get(path, T::class, method.name)

    fun <T : Controller> get(path: String, controller: KClass<T>, method: KFunction2<T, HttpCall, Unit>) =
        add(Method.GET, path, ControllerHandler(controller, method.name), middleware).name(method.name.toKebabCase())

    fun get(path: String, controller: KClass<out Controller>, method: String = DEFAULT_GET_METHOD) =
        add(Method.GET, path, ControllerHandler(controller, method), middleware).name(method.toKebabCase())

    inline fun <reified T : Controller> post(path: String = "", method: String = DEFAULT_POST_METHOD) =
        post(path, T::class, method)

    fun post(controller: KClass<out Controller>, method: String = DEFAULT_POST_METHOD) = post("", controller, method)

    inline fun <reified T : Controller> post(method: KFunction2<T, HttpCall, Unit>) =
        post("", T::class, method.name)

    fun <T : Controller> post(controller: KClass<T>, method: KFunction2<T, HttpCall, Unit>) =
        post("", controller, method.name)

    inline fun <reified T : Controller> post(path: String, method: KFunction2<T, HttpCall, Unit>) =
        post(path, T::class, method.name)

    fun <T : Controller> post(path: String, controller: KClass<T>, method: KFunction2<T, HttpCall, Unit>) =
        add(Method.POST, path, ControllerHandler(controller, method.name), middleware).name(method.name.toKebabCase())

    fun post(path: String, controller: KClass<out Controller>, method: String = DEFAULT_POST_METHOD) =
        add(Method.POST, path, ControllerHandler(controller, method), middleware).name(method)

    inline fun <reified T : Controller> delete(path: String = "", method: String = DEFAULT_DELETE_METHOD) =
        delete(path, T::class, method)

    fun delete(controller: KClass<out Controller>, method: String = DEFAULT_DELETE_METHOD) =
        delete("", controller, method)

    inline fun <reified T : Controller> delete(method: KFunction2<T, HttpCall, Unit>) =
        delete("", T::class, method.name)

    fun <T : Controller> delete(controller: KClass<T>, method: KFunction2<T, HttpCall, Unit>) =
        delete("", controller, method.name)

    inline fun <reified T : Controller> delete(path: String, method: KFunction2<T, HttpCall, Unit>) =
        delete(path, T::class, method.name)

    fun delete(path: String, controller: KClass<out Controller>, method: String = DEFAULT_DELETE_METHOD) =
        add(Method.DELETE, path, ControllerHandler(controller, method), middleware).name(method.toKebabCase())

    fun <T : Controller> delete(path: String, controller: KClass<T>, method: KFunction2<T, HttpCall, Unit>) =
        add(Method.DELETE, path, ControllerHandler(controller, method.name), middleware).name(method.name.toKebabCase())

    inline fun <reified T : Controller> patch(path: String = "", method: String = DEFAULT_PATCH_METHOD) =
        patch(path, T::class, method)

    fun patch(controller: KClass<out Controller>, method: String = DEFAULT_PATCH_METHOD) =
        patch("", controller, method)

    inline fun <reified T : Controller> patch(method: KFunction2<T, HttpCall, Unit>) =
        patch("", T::class, method.name)

    fun <T : Controller> patch(controller: KClass<T>, method: KFunction2<T, HttpCall, Unit>) =
        patch("", controller, method.name)

    inline fun <reified T : Controller> patch(path: String, method: KFunction2<T, HttpCall, Unit>) =
        patch(path, T::class, method.name)

    fun patch(path: String, controller: KClass<out Controller>, method: String = DEFAULT_PATCH_METHOD) =
        add(Method.PATCH, path, ControllerHandler(controller, method), middleware).name(method.toKebabCase())

    fun <T : Controller> patch(path: String, controller: KClass<T>, method: KFunction2<T, HttpCall, Unit>) =
        add(Method.PATCH, path, ControllerHandler(controller, method.name), middleware).name(method.name.toKebabCase())

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
        if (childGroup.name.isEmpty()) {
            val derivedName = prefix.replace("/", ".")
            childGroup.name(derivedName)
        }
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

    inline fun <reified T : Controller> resources(
        path: String = "/",
        only: List<String> = listOf(
            DEFAULT_GET_METHOD,
            DEFAULT_POST_METHOD,
            DEFAULT_PATCH_METHOD,
            DEFAULT_DELETE_METHOD,
            "edit", "new", "show"
        ),
        except: List<String> = emptyList()
    ): RouteGroup {
        return group(prefix = path) {
            if (only.contains(DEFAULT_GET_METHOD) && !except.contains(DEFAULT_GET_METHOD)) {
                get<T>().name(DEFAULT_GET_METHOD)
            }
            if (only.contains("edit") && !except.contains("edit")) {
                get<T>("<id>/edit", "edit").name("edit")
            }
            if (only.contains("new") && !except.contains("new")) {
                get<T>("new", "new").name("new")
            }
            if (only.contains("show") && !except.contains("show")) {
                get<T>("<id>", "show").name("show")
            }
            if (only.contains(DEFAULT_POST_METHOD) && !except.contains(DEFAULT_POST_METHOD)) {
                post<T>().name(DEFAULT_POST_METHOD)
            }
            if (only.contains(DEFAULT_PATCH_METHOD) && !except.contains(DEFAULT_PATCH_METHOD)) {
                patch<T>().name(DEFAULT_PATCH_METHOD)
            }
            if (only.contains(DEFAULT_DELETE_METHOD) && !except.contains(DEFAULT_DELETE_METHOD)) {
                delete<T>().name(DEFAULT_DELETE_METHOD)
            }
        }
    }

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
