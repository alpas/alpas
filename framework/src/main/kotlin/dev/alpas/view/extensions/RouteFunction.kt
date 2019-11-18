package dev.alpas.view.extensions

import com.mitchellbosecke.pebble.extension.Function
import com.mitchellbosecke.pebble.template.EvaluationContext
import com.mitchellbosecke.pebble.template.PebbleTemplate
import dev.alpas.http.HttpCall
import dev.alpas.routing.Route

private val EvaluationContext.call: HttpCall
    get() {
        return this.getVariable("call") as HttpCall
    }

internal class RouteFunction() : Function {

    override fun getArgumentNames(): List<String> {
        return listOf("name", "params")
    }

    @Suppress("UNCHECKED_CAST")
    override fun execute(args: Map<String, Any>?, self: PebbleTemplate, ctx: EvaluationContext, line: Int): String {
        try {
            val name = args?.get("name") as String?
                ?: throw Exception("route() function requires the name of the route.")
            val urlGenerator= ctx.call.urlGenerator
            return urlGenerator.route(name, args?.get("params") as? Map<String, Any>)
        } catch (e: Exception) {
            throw Exception("${e.message}. Called from (${self.name} line no. $line)")
        }
    }
}

internal class HasRouteFunction() : Function {

    override fun getArgumentNames(): List<String> {
        return listOf("name")
    }

    @Suppress("UNCHECKED_CAST")
    override fun execute(args: Map<String, Any>?, self: PebbleTemplate, ctx: EvaluationContext, line: Int): Boolean {
        try {
            val name = args?.get("name") as String?
                ?: throw Exception("route() function requires the name of the route.")
            val urlGenerator = ctx.call.urlGenerator
            return urlGenerator.hasRoute(name)
        } catch (e: Exception) {
            throw Exception("${e.message}. Called from (${self.name} line no. $line)")
        }
    }
}

internal class RouteIsFunction : Function {
    override fun getArgumentNames(): List<String> {
        return listOf("name")
    }

    @Suppress("UNCHECKED_CAST")
    override fun execute(args: Map<String, Any>?, self: PebbleTemplate, ctx: EvaluationContext, line: Int): Boolean {
        try {
            val name = args?.get("name") as? String
                ?: throw Exception("routeIs() function requires the name of the route to check against.")
            val route = ctx.getVariable("_route") as Route
            return name == route.name
        } catch (e: Exception) {
            throw Exception("${e.message}. Called from (${self.name} line no. $line)")
        }
    }
}

internal class RouteIsOneOfFunction : Function {
    override fun getArgumentNames(): List<String> {
        return listOf("names")
    }

    @Suppress("UNCHECKED_CAST")
    override fun execute(args: Map<String, Any>?, self: PebbleTemplate, ctx: EvaluationContext, line: Int): Boolean {
        try {
            val names = args?.get("names") as? List<String>
                ?: throw Exception("routeIs() function requires at least one of the names of the route to check against.")
            val route = ctx.getVariable("_route") as Route
            return names.contains(route.name)
        } catch (e: Exception) {
            throw Exception("${e.message}. Called from (${self.name} line no. $line)")
        }
    }
}
