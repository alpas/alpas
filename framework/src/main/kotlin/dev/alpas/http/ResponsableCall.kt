package dev.alpas.http

import dev.alpas.JsonSerializable
import dev.alpas.exceptions.httpExceptionFor
import dev.alpas.validation.ErrorBag
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
        response = StringResponse(payload?.toString(), statusCode)
        return this
    }

    fun <T : Map<*, *>> replyAsJson(payload: T, statusCode: Int = HttpStatus.OK_200): ResponsableCall {
        response = JsonResponse(payload, statusCode)
        return this
    }

    fun replyAsJson(payload: JsonSerializable, statusCode: Int = HttpStatus.OK_200): ResponsableCall {
        response = JsonResponse(payload, statusCode)
        return this
    }

    fun render(response: JsonResponse) {
        this.response = response
    }

    fun render(response: Response) {
        this.response = response
    }

    fun render(
        templateName: String,
        arg: Pair<String, Any?>,
        statusCode: Int = HttpStatus.OK_200
    ): ViewResponse {
        return render(templateName, mapOf(arg), statusCode)
    }

    fun render(
        templateName: String,
        args: Map<String, Any?>? = null,
        statusCode: Int = HttpStatus.OK_200
    ): ViewResponse {
        return ViewResponse(templateName.replace(".", "/"), args, statusCode)
            .also { this.response = it }
    }

    fun acknowledge(statusCode: Int = HttpStatus.NO_CONTENT_204): ResponsableCall {
        response = AcknowledgementResponse(statusCode)
        return this
    }

    fun render(
        templateName: String,
        args: MutableMap<String, Any?>? = null,
        statusCode: Int = 200,
        block: ArgsBuilder.() -> Unit
    ): ViewResponse {
        val builder = ArgsBuilder(args ?: mutableMapOf()).also(block)
        return render(templateName, builder.map(), statusCode)
    }

    fun json(
        args: MutableMap<String, Any?>? = null,
        statusCode: Int = 200,
        block: ArgsBuilder.() -> Unit
    ): ResponsableCall {
        val builder = ArgsBuilder(args ?: mutableMapOf()).also(block)
        return replyAsJson(builder.map(), statusCode)
    }

    fun contentType(type: String): ResponsableCall
    fun asHtml() = contentType(HTML_CONTENT_TYPE)
    fun asJson() = contentType(JSON_CONTENT_TYPE)

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
    fun status(code: Int): ResponsableCall
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

class ArgsBuilder(private val assignments: MutableMap<String, Any?> = mutableMapOf()) {
    infix fun String.to(argument: Any?) {
        assignments[this] = argument
    }

    internal fun map(): Map<String, Any?> {
        return assignments
    }
}
