package dev.alpas.http

import dev.alpas.filterNotNullValues
import dev.alpas.isOneOf
import dev.alpas.routing.RouteResult
import java.util.concurrent.atomic.AtomicBoolean

interface RequestParamsBagContract {
    // all parameters including query params, form params, and route params
    val params: Map<String, List<Any>?>?
    val routeParams: Map<String, List<Any>?>?
    val queryParams: Map<String, List<Any>?>?

    fun param(key: String): Any? {
        return params?.get(key)?.firstOrNull()
    }

    fun stringParam(key: String): String? {
        return params?.get(key)?.firstOrNull()?.toString()
    }

    fun intParam(key: String): Int? {
        return params?.get(key)?.firstOrNull()?.toString()?.toInt()
    }

    fun longParam(key: String): Long? {
        return params?.get(key)?.firstOrNull()?.toString()?.toLong()
    }

    fun boolParam(key: String): Boolean? {
        return params?.get(key)?.firstOrNull()?.toString()?.toBoolean()
    }

    fun params(key: String): List<Any>? {
        return paramList(key)
    }

    fun paramList(key: String): List<Any>? {
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

    fun params(key: String, vararg keys: String): Map<String, Any?> {
        return params
            ?.filterKeys { it.isOneOf(key, *keys) }
            ?.filterNotNullValues() // remove nulls
            ?.map { it.key to it.value.firstOrNull() } // only return the first value from the list
            ?.toMap()
            ?: emptyMap()
    }

    fun paramsExcept(key: String, vararg keys: String): Map<String, Any?> {
        return params
            ?.filterKeys { it.isOneOf(key, *keys) }
            ?.filterNotNullValues() // remove nulls
            ?.map { it.key to it.value.firstOrNull() } // only return the first value from the list
            ?.toMap()
            ?: emptyMap()
    }

    fun combineJsonBodyWithParams()
}

class RequestParamsBag(private val request: RequestableCall, private val route: RouteResult) :
    RequestParamsBagContract {
    private val combineJsonBodyWithParams: AtomicBoolean = AtomicBoolean(false)
    override val params by lazy {
        // merge both routeParams map and query routeParams
        val paramsMap = routeParams.toMutableMap()
        val requestParams = requestParams

        requestParams.forEach { (key, param) ->
            paramsMap[key] = paramsMap[key]?.plus(param) ?: param
        }

        if (combineJsonBodyWithParams.get()) {
            request.jsonBody?.forEach { (key, param) ->
                val actualParam = when (param) {
                    is List<*> -> param.map { it }
                    else -> listOf(param)
                }.filterNotNull()

                paramsMap[key] = paramsMap[key]?.plus(actualParam) ?: actualParam
            }
        }
        paramsMap.toMap()
    }

    private val requestParams by lazy {
        if (request.isMultipartFormData) {
            request.multipartParams
        } else {
            request.jettyRequest.parameterMap.mapValues { it.value.toList() }
        }
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

    override fun combineJsonBodyWithParams() {
        combineJsonBodyWithParams.set(true)
    }
}
