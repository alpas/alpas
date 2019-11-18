package dev.alpas.http.jetty

import dev.alpas.Application
import dev.alpas.ChildContainer
import dev.alpas.Middleware
import dev.alpas.Pipeline
import dev.alpas.exceptions.MethodNotAllowedException
import dev.alpas.exceptions.NotFoundHttpException
import dev.alpas.http.HttpCall
import dev.alpas.http.Method
import dev.alpas.http.StaticAssetHandler
import dev.alpas.make
import dev.alpas.routing.Route
import dev.alpas.routing.RouteMatchStatus
import dev.alpas.routing.Router
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

class AlpasServlet(
    private val app: Application,
    private val routeEntryMiddlewareGroups: Map<String, Iterable<KClass<out Middleware<HttpCall>>>>,
    private val serverEntryMiddleware: Iterable<KClass<out Middleware<HttpCall>>>
) : HttpServlet() {
    private val staticHandler by lazy { StaticAssetHandler(app) }
    override fun service(req: HttpServletRequest, resp: HttpServletResponse) {
        val router = app.make<Router>()
        val route = router.routeFor(methodName = req.method(), uri = req.requestURI)
        ChildContainer(app).use { container ->
            val call = HttpCall(container, req, resp, route)
            app.recordLastCall(call)
            try {
                if (!staticHandler.handle(call)) {
                    call.sendCallThroughServerEntryMiddleware().then(::matchRoute)
                    call.close()
                }
            } catch (e: Exception) {
                call.drop(e)
            }
        }
    }

    private fun matchRoute(call: HttpCall) {
        when (call.route.status()) {
            RouteMatchStatus.SUCCESS -> call.dispatchToRouteHandler(call.route.target())
            RouteMatchStatus.METHOD_NOT_ALLOWED -> {
                throw MethodNotAllowedException(
                    "Method ${call.method} is not allowed for this operation. Only ${call.route.allowedMethods().joinToString(
                        ", "
                    ).toUpperCase()} methods are allowed."
                )
            }
            else -> throw NotFoundHttpException()
        }
    }

    private fun HttpCall.dispatchToRouteHandler(route: Route) {
        val groupMiddleware = mutableListOf<Middleware<HttpCall>>()
        route.middlewareGroups.forEach {
            groupMiddleware.addAll(makeMiddleware(routeEntryMiddlewareGroups[it]))
        }
        val middleware = groupMiddleware.plus(makeMiddleware(route.middleware))
        Pipeline<HttpCall>().send(this).through(middleware).then {
            route.handle(it)
        }
    }

    @Suppress("RemoveExplicitTypeArguments")
    private fun makeMiddleware(middleware: Iterable<KClass<out Middleware<HttpCall>>>?): Iterable<Middleware<HttpCall>> {
        return middleware?.filter { app.shouldLoadMiddleware(it) }?.map { it.createInstance() } ?: emptyList()
    }

    private fun HttpCall.sendCallThroughServerEntryMiddleware(): Pipeline<HttpCall> {
        return Pipeline<HttpCall>().send(this).through(makeMiddleware(serverEntryMiddleware))
    }
}

private fun HttpServletRequest.method(): String {
    if (method != Method.POST.name) {
        return method
    }
    return parameterMap["_method"]?.firstOrNull()?.toUpperCase() ?: method
}
