package dev.alpas.http.jetty

import dev.alpas.*
import dev.alpas.exceptions.MethodNotAllowedException
import dev.alpas.exceptions.NotFoundHttpException
import dev.alpas.http.HttpCall
import dev.alpas.http.HttpCallHook
import dev.alpas.http.Method
import dev.alpas.http.StaticAssetHandler
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
            val callHooks = app.callHooks.map {
                it.createInstance()
            }
            val call = HttpCall(container, req, resp, route, callHooks).also {
                if (app.env.inTestMode) {
                    app.recordLastCall(it)
                }
            }

            try {
                if (!staticHandler.handle(call)) {

                    call.logger.debug { "Booting ${callHooks.size} HttpCall hooks" }
                    callHooks.forEach { it.boot(call) }
                    call.logger.debug { "Registering ${callHooks.size} HttpCall hooks" }
                    callHooks.forEach { it.register(call) }

                    call.sendCallThroughServerEntryMiddleware().then { matchRoute(it, callHooks) }

                    call.logger.debug { "Clean closing ${callHooks.size} HttpCall hooks" }
                    callHooks.forEach { it.beforeClose(call, true) }
                    call.close()
                }
            } catch (e: Exception) {
                call.logger.debug { "Unclean closing ${callHooks.size} HttpCall hooks" }
                callHooks.forEach { it.beforeClose(call, false) }
                call.drop(e)
            }
        }
    }

    private fun matchRoute(call: HttpCall, callHooks: List<HttpCallHook>) {
        when (call.route.status()) {
            RouteMatchStatus.SUCCESS -> call.dispatchToRouteHandler(call.route.target(), callHooks)
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

    private fun HttpCall.dispatchToRouteHandler(route: Route, callHooks: List<HttpCallHook>) {
        val groupMiddleware = mutableListOf<Middleware<HttpCall>>()
        route.middlewareGroups.forEach {
            groupMiddleware.addAll(makeMiddleware(routeEntryMiddlewareGroups[it]))
        }
        val middleware = groupMiddleware.plus(makeMiddleware(route.middleware))
        Pipeline<HttpCall>().send(this).through(middleware).then { call ->
            logger.debug { "Calling before route handle hook for ${callHooks.size} hooks" }
            callHooks.forEach { it -> it.beforeRouteHandle(call, route) }
            route.handle(call)
        }
    }

    @Suppress("RemoveExplicitTypeArguments")
    private fun makeMiddleware(middleware: Iterable<KClass<out Middleware<HttpCall>>>?): Iterable<Middleware<HttpCall>> {
        return middleware?.filter { app.shouldLoadMiddleware(it) }?.map { it.createInstance() } ?: emptyList()
    }

    private fun HttpCall.sendCallThroughServerEntryMiddleware(): Pipeline<HttpCall> {
        return Pipeline<HttpCall>().send(this).through(makeMiddleware(serverEntryMiddleware))
    }

    private fun HttpServletRequest.method(): String {
        // If method is not a POST method, no need to check it further. We just return the method as it is.
        // If method spoofing is not allowed we return the method name as it is.
        if (method != Method.POST.name || !app.config<AppConfig>().allowMethodSpoofing) {
            return method
        }
        return parameterMap["_method"]?.firstOrNull()?.toUpperCase() ?: method
    }
}
