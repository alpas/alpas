package dev.alpas.http

import dev.alpas.JsonSerializable
import dev.alpas.exceptions.httpExceptionFor
import dev.alpas.validation.ErrorBag
import dev.alpas.validation.SharedDataBag
import org.eclipse.jetty.http.HttpStatus
import java.time.Duration
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse

interface ResponsableCall {
    val errorBag: ErrorBag
    val servletResponse: HttpServletResponse
    var response: Response

    fun addHeaders(headers: Map<String, String>): ResponsableCall

    fun addHeader(header: Pair<String, String>, vararg headers: Pair<String, String>): ResponsableCall {
        return addHeaders(mapOf(header) + headers)
    }

    fun addHeader(key: String, value: String): ResponsableCall

    fun <T> reply(payload: T? = null, statusCode: Int = HttpStatus.OK_200): ResponsableCall {
        response = StringResponse(payload?.toString())
        servletResponse.status = statusCode
        asHtml()
        return this
    }

    fun <T : Map<*, *>> replyAsJson(payload: T, statusCode: Int = HttpStatus.OK_200): ResponsableCall {
        response = JsonResponse(payload)
        status(statusCode)
        asJson()
        return this
    }

    fun replyAsJson(payload: JsonSerializable, statusCode: Int = HttpStatus.OK_200): ResponsableCall {
        response = JsonResponse(payload)
        status(statusCode)
        asJson()
        return this
    }

    fun render(response: JsonResponse, statusCode: Int = HttpStatus.OK_200) {
        this.response = response
        status(statusCode)
        asJson()
    }

    fun render(response: Response, statusCode: Int = HttpStatus.OK_200) {
        this.response = response
        status(statusCode)
        asHtml()
    }

    fun render(
        templateName: String,
        arg: Pair<String, Any?>,
        statusCode: Int = HttpStatus.OK_200
    ): ViewResponse {
        return this.render(templateName, mapOf(arg), statusCode)
    }

    fun render(
        templateName: String,
        args: Map<String, Any?>? = null,
        statusCode: Int = HttpStatus.OK_200
    ): ViewResponse {
        return ViewResponse(templateName.replace(".", "/"), args)
            .also { render(it, statusCode) }
    }

    fun acknowledge(statusCode: Int = HttpStatus.NO_CONTENT_204): ResponsableCall {
        servletResponse.status = statusCode
        response = StringResponse("")
        asJson()
        return this
    }

    fun contentType(type: String) {
        servletResponse.contentType = type
    }

    fun asHtml() {
        contentType("text/html; charset=UTF-8")
    }

    fun asJson() {
        contentType("application/json; charset=UTF-8")
    }

    fun abort(statusCode: Int, message: String? = null, headers: Map<String, String> = emptyMap()): Nothing {
        throw httpExceptionFor(statusCode, message, headers)
    }

    fun abortUnless(
        condition: Boolean,
        statusCode: Int,
        message: String? = null,
        headers: Map<String, String> = emptyMap()
    ) {
        if (!condition) {
            abort(statusCode, message, headers)
        }
    }

    fun abortIf(
        condition: Boolean,
        statusCode: Int,
        message: String? = null,
        headers: Map<String, String> = emptyMap()
    ) {
        if (condition) {
            abort(statusCode, message, headers)
        }
    }

    fun shared(key: String): Any?
    fun share(pair: Pair<String, Any?>, vararg pairs: Pair<String, Any>)

    fun finalize(call: HttpCall)

    fun status(code: Int): ResponsableCall {
        servletResponse.status = code
        return this
    }
}

class Responsable internal constructor(override val servletResponse: HttpServletResponse) : ResponsableCall {
    private var headers = mutableMapOf<String, String>()
    override val errorBag: ErrorBag by lazy { ErrorBag() }
    override lateinit var response: Response

    private val sharedData by lazy { SharedDataBag() }

    override fun addHeaders(headers: Map<String, String>): ResponsableCall {
        this.headers.putAll(headers)
        return this
    }

    override fun addHeader(key: String, value: String): ResponsableCall {
        this.headers[key] = value
        return this
    }

    override fun finalize(call: HttpCall) {
        saveCookies(call)
        copyHeaders()
        copyContent(call)
    }

    private fun saveCookies(call: HttpCall) {
        call.cookie.outgoingCookies.forEach {
            servletResponse.addCookie(it)
        }
    }

    private fun copyContent(call: HttpCall) {
        renderView(call)
    }

    private fun renderView(call: HttpCall) {
        val context = RenderContext(call, sharedData).also {
            call.onBeforeRender(it)
        }
        try {
            response.render(context) { copyTo(servletResponse.outputStream) }
        } catch (e: Exception) {
            response.renderException(e, context) { copyTo(servletResponse.outputStream) }
        }
    }

    private fun copyHeaders() {
        headers.forEach { (key, value) ->
            servletResponse.addHeader(key, value)
        }
    }

    override fun share(pair: Pair<String, Any?>, vararg pairs: Pair<String, Any>) {
        sharedData.add(pair, *pairs)
    }

    override fun shared(key: String): Any? {
        return sharedData[key]
    }
}

private fun makeCookie(
    name: String,
    value: String?,
    lifetime: Duration = Duration.ofSeconds(-1),
    path: String? = null,
    domain: String? = null,
    secure: Boolean = false,
    httpOnly: Boolean = true
): Cookie {
    return Cookie(name, value).also { cookie ->
        cookie.maxAge = lifetime.seconds.toInt()
        path?.let { cookie.path = it }
        domain?.let { cookie.domain = it }
        cookie.secure = secure
        cookie.isHttpOnly = httpOnly
    }
}
