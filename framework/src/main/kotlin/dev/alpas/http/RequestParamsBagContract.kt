package dev.alpas.http

import dev.alpas.filterNotNullValues
import dev.alpas.orAbort
import dev.alpas.routing.RouteResult
import org.eclipse.jetty.http.HttpStatus
import java.net.URLDecoder
import java.util.concurrent.atomic.AtomicBoolean

interface RequestParamsBagContract {
    // all parameters including query params, form params, and route params
    val params: Map<String, List<Any>?>?
    val routeParams: Map<String, List<Any>?>?
    val queryParams: Map<String, List<Any>?>?

    fun param(key: String): Any? {
        return params?.get(key)?.firstOrNull()
    }

    fun stringOrNull(key: String): String? {
        return params?.get(key)?.firstOrNull()?.toString()
    }

    @Deprecated("Deprecated", ReplaceWith("stringOrNull(key)"))
    fun stringParamOrNull(key: String): String? {
        return stringOrNull(key)
    }

    fun string(key: String, message: String? = null, statusCode: Int = HttpStatus.NOT_FOUND_404): String {
        return stringOrNull(key).orAbort(message, statusCode)
    }

    @Deprecated("Deprecated", ReplaceWith("string(key, message, statusCode)"))
    fun stringParam(key: String, message: String? = null, statusCode: Int = HttpStatus.NOT_FOUND_404): String {
        return string(key, message, statusCode)
    }

    fun intOrNull(key: String): Int? {
        return params?.get(key)?.firstOrNull()?.toString()?.toInt()
    }

    @Deprecated("Deprecated", ReplaceWith("intOrNull(key)"))
    fun intParamOrNull(key: String): Int? {
        return intOrNull(key)
    }

    fun int(key: String, message: String? = null, statusCode: Int = HttpStatus.NOT_FOUND_404): Int {
        return intOrNull(key).orAbort(message, statusCode)
    }

    @Deprecated("Deprecated", ReplaceWith("int(key, message, statusCode)"))
    fun intParam(key: String, message: String? = null, statusCode: Int = HttpStatus.NOT_FOUND_404): Int {
        return int(key, message, statusCode)
    }

    fun longOrNull(key: String): Long? {
        return params?.get(key)?.firstOrNull()?.toString()?.toLong()
    }

    @Deprecated("Deprecated", ReplaceWith("longOrNull(key)"))
    fun longParamOrNull(key: String): Long? {
        return longOrNull(key)
    }

    fun long(key: String, message: String? = null, statusCode: Int = HttpStatus.NOT_FOUND_404): Long {
        return longOrNull(key).orAbort(message, statusCode)
    }

    @Deprecated("Deprecated", ReplaceWith("long(key, message, statusCode)"))
    fun longParam(key: String, message: String? = null, statusCode: Int = HttpStatus.NOT_FOUND_404): Long {
        return long(key, message, statusCode)
    }

    fun boolOrNull(key: String): Boolean? {
        return params?.get(key)?.firstOrNull()?.toString()?.toBoolean()
    }

    @Deprecated("Deprecated", ReplaceWith("boolOrNull(key)"))
    fun boolParamOrNull(key: String): Boolean? {
        return boolOrNull(key)
    }

    fun bool(key: String, message: String? = null, statusCode: Int = HttpStatus.NOT_FOUND_404): Boolean {
        return boolOrNull(key).orAbort(message, statusCode)
    }

    @Deprecated("Deprecated", ReplaceWith("bool(key, message, statusCode)"))
    fun boolParam(key: String, message: String? = null, statusCode: Int = HttpStatus.NOT_FOUND_404): Boolean? {
        return bool(key, message, statusCode)
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

    fun has(key: String): Boolean {
        return param(key) != null
    }

    fun filled(key: String): Boolean {
        val value = param(key) ?: return false
        if (value as? Boolean != null) {
            return value
        }
        return (value as? String)?.isNotEmpty() ?: true
    }

    fun params(vararg keys: String, firstValueOnly: Boolean = true): Map<String, Any?> {
        return params?.let {
            if (keys.isEmpty()) it else it.filterKeys { key -> key in keys }
        }?.filterNotNullValues() // remove nulls
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

        bodyFormParams.forEach { (key, param) ->
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

    private val bodyFormParams by lazy {
        if (request.isUrlEncodedForm && request.body.isNotBlank()) {
            request.body
                .split("&")
                .map { it.split("=", limit = 2) }
                .groupBy(
                    { it[0] },
                    { if (it.size > 1) URLDecoder.decode(it[1], request.encoding.name()) else "" }
                ).mapValues { it.value.toList() }
        } else {
            emptyMap()
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
