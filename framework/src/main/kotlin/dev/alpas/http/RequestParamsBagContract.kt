package dev.alpas.http

import dev.alpas.filterNotNullValues
import dev.alpas.isOneOf
import dev.alpas.routing.RouteResult

interface RequestParamsBagContract {
    // all parameters including query params, form params, and route params
    val params: Map<String, List<Any>?>?
    val routeParams: Map<String, List<Any>?>?
    val queryParams: Map<String, List<Any>?>?

    fun param(key: String): Any? {
        return params?.get(key)?.firstOrNull()
    }

    fun paramAsString(key: String): String? {
        return params?.get(key)?.firstOrNull()?.toString()
    }

    fun paramAsInt(key: String): Int? {
        return params?.get(key)?.firstOrNull()?.toString()?.toInt()
    }

    fun paramAsLong(key: String): Long? {
        return params?.get(key)?.firstOrNull()?.toString()?.toLong()
    }

    fun paramAsBool(key: String): Boolean? {
        return params?.get(key)?.firstOrNull()?.toString()?.toBoolean()
    }

    fun params(key: String): List<Any>? {
        return params?.get(key)
    }

    fun routeParam(key: String): Any? {
        return routeParams?.get(key)?.firstOrNull()
    }

    fun routeParams(key: String): List<Any>? {
        return routeParams?.get(key)
    }

    fun queryParam(key: String): Any? {
        return queryParams?.get(key)?.firstOrNull()
    }

    fun queryParams(key: String): List<Any>? {
        return queryParams?.get(key)
    }

    fun onlyParams(key: String, vararg keys: String): Map<String, List<Any>> {
        return params?.filterKeys {
            it.isOneOf(key, *keys)
        }?.filterNotNullValues() ?: emptyMap()
    }

    fun paramsExcept(key: String, vararg keys: String): Map<String, List<Any>> {
        return params?.filterKeys {
            !it.isOneOf(key, *keys)
        }?.filterNotNullValues() ?: emptyMap()
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

    override val queryParams: Map<String, List<Any>?> by lazy {
        request.jettyRequest.queryParameters
    }
}
