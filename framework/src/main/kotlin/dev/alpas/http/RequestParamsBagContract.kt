package dev.alpas.http

import dev.alpas.filterNotNullValues
import dev.alpas.orAbort
import dev.alpas.routing.RouteResult
import org.eclipse.jetty.http.HttpStatus
import java.util.concurrent.atomic.AtomicBoolean

interface RequestParamsBagContract {
    // all parameters including query params, form params, and route params
    val params: Map<String, List<Any>?>?
    val routeParams: Map<String, List<Any>?>?
    val queryParams: Map<String, List<Any>?>?

    fun param(key: String): Any? {
        return params?.get(key)?.firstOrNull()
    }

    fun stringParamOrNull(key: String): String? {
        return params?.get(key)?.firstOrNull()?.toString()
    }

    fun stringParam(key: String, message: String? = null, statusCode: Int = HttpStatus.NOT_FOUND_404): String {
        return stringParamOrNull(key).orAbort(message, statusCode)
    }

    fun intParamOrNull(key: String): Int? {
        return params?.get(key)?.firstOrNull()?.toString()?.toInt()
    }

    fun intParam(key: String, message: String? = null, statusCode: Int = HttpStatus.NOT_FOUND_404): Int? {
        return intParamOrNull(key).orAbort(message, statusCode)
    }

    fun longParamOrNull(key: String): Long? {
        return params?.get(key)?.firstOrNull()?.toString()?.toLong()
    }

    fun longParam(key: String, message: String? = null, statusCode: Int = HttpStatus.NOT_FOUND_404): Long {
        return longParamOrNull(key).orAbort()
    }

    fun boolParamOrNull(key: String): Boolean? {
        return params?.get(key)?.firstOrNull()?.toString()?.toBoolean()
    }

    fun boolParam(key: String, message: String? = null, statusCode: Int = HttpStatus.NOT_FOUND_404): Boolean? {
        return boolParamOrNull(key).orAbort(message, statusCode)
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

    fun params(vararg keys: String, firstValueOnly: Boolean = true): Map<String, Any?> {
        return params
            ?.filterKeys { it in keys }
            ?.filterNotNullValues() // remove nulls
            ?.let { filteredParams ->
                if (firstValueOnly) {
                    filteredParams.map { it.key to it.value.firstOrNull() }.toMap()
                } else {
                    filteredParams
                }
            } ?: emptyMap()
    }

    fun paramsExcept(vararg keys: String, firstValueOnly: Boolean = true): Map<String, Any?> {
        return params
            ?.filterKeys { it !in keys }
            ?.filterNotNullValues() // remove nulls
            ?.let { filteredParams ->
                if (firstValueOnly) {
                    filteredParams.map { it.key to it.value.firstOrNull() }.toMap()
                } else {
                    filteredParams
                }
            } ?: emptyMap()
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
