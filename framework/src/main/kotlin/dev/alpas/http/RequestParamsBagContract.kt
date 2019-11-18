package dev.alpas.http

import dev.alpas.routing.RouteResult

interface RequestParamsBagContract {
    // all parameters including query params, form params, and route params
    val params: Map<String, List<Any>>
    val routeParams: Map<String, List<Any>>

    fun param(key: String): Any? {
        return params[key]?.firstOrNull()
    }

    fun paramAsString(key: String): String? {
        return params[key]?.firstOrNull()?.toString()
    }

    fun paramAsInt(key: String): Int? {
        return params[key]?.firstOrNull()?.toString()?.toInt()
    }

    fun paramAsLong(key: String): Long? {
        return params[key]?.firstOrNull()?.toString()?.toLong()
    }

    fun paramAsBool(key: String): Boolean? {
        return params[key]?.firstOrNull()?.toString()?.toBoolean()
    }

    fun params(key: String): List<Any>? {
        return params[key]
    }

    fun routeParam(key: String): Any? {
        return routeParams[key]?.firstOrNull()
    }

    fun routeParams(key: String): List<Any>? {
        return if (routeParams.isNotEmpty()) routeParams[key] else null
    }

    fun only(vararg key: String): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        key.forEach { k ->
            param(k)?.let { value -> map[k] = value }
        }
        return map
    }

    fun only(vararg key: String, default: () -> Map<String, Any>): Map<String, Any> {
        return only(*key).let {
            if (it.isEmpty()) default() else it
        }
    }
}

class RequestParamsBag(private val request: Requestable, private val route: RouteResult) :
    RequestParamsBagContract {
    override val params by lazy {
        // merge both routeParams map and query routeParams
        val paramsMap = routeParams.toMutableMap()
        val requestParams = request.jettyRequest.parameterMap.mapValues { it.value.toList() }

        requestParams.forEach { (key, param) ->
            paramsMap[key] = paramsMap[key]?.plus(param) ?: param
        }
        paramsMap.toMap()
    }

    override val routeParams by lazy {
        mutableMapOf<String, List<Any>>().also {
            for (idx in 0 until route.params()) {
                val arrayOf = listOf(route.paramValue(idx))
                it[route.paramName(idx)] = arrayOf
            }
        }.toMap()
    }
}
