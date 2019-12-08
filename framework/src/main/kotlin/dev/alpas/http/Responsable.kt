package dev.alpas.http

import dev.alpas.JsonSerializable
import dev.alpas.charset
import dev.alpas.exceptions.toHttpException
import dev.alpas.make
import dev.alpas.session.OLD_INPUTS_KEY
import dev.alpas.session.VALIDATION_ERRORS_KEY
import dev.alpas.stackTraceString
import dev.alpas.validation.ErrorBag
import dev.alpas.view.View
import dev.alpas.view.ViewRenderer
import org.eclipse.jetty.http.HttpStatus
import java.io.InputStream
import java.time.Duration
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse

interface Responsable {
    val errorBag: ErrorBag
    val servletResponse: HttpServletResponse
    var responseStream: InputStream
    var view: View

    fun addHeaders(headers: Map<String, String>): Responsable

    fun addHeader(key: String, value: String): Responsable

    fun <T> reply(payload: T? = null, statusCode: Int = HttpStatus.OK_200): Responsable {
        responseStream = (payload?.toString() ?: "").byteInputStream(servletResponse.charset())
        servletResponse.status = statusCode
        asHtml()
        return this
    }

    fun <T> replyAsJson(payload: T? = null, statusCode: Int = HttpStatus.OK_200): Responsable {
        reply(payload, statusCode)
        asJson()
        return this
    }

    fun replyAsJson(obj: JsonSerializable, statusCode: Int = HttpStatus.OK_200): Responsable {
        return replyAsJson(obj.toJson(), statusCode)
    }

    fun render(view: View, statusCode: Int = HttpStatus.OK_200) {
        this.view = view
        status(statusCode)
        asHtml()
    }

    fun render(templateName: String, args: Map<String, Any?>? = null, statusCode: Int = HttpStatus.OK_200): View {
        return View(templateName.replace(".", "/"), args).also { render(it, statusCode) }
    }

    fun acknowledge(statusCode: Int = HttpStatus.NO_CONTENT_204): Responsable {
        servletResponse.status = statusCode
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
        throw statusCode.toHttpException(message, headers)
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

    fun shareWithView(key: String, value: Any?)

    fun finalize(call: HttpCall)

    fun status(code: Int): Responsable {
        servletResponse.status = code
        return this
    }

    fun addCookie(
        name: String,
        value: String?,
        lifetime: Duration = Duration.ofSeconds(-1),
        path: String? = null,
        domain: String? = null,
        secure: Boolean = false,
        httpOnly: Boolean = true
    ): Responsable

    fun addCookie(cookie: Cookie): Responsable
    fun forgetCookie(name: String, path: String? = null, domain: String? = null): Responsable
}

class Response internal constructor(override val servletResponse: HttpServletResponse) : Responsable {
    private var headers = mutableMapOf<String, String>()
    private var cookies = mutableListOf<Cookie>()
    override val errorBag: ErrorBag by lazy { ErrorBag() }
    override lateinit var view: View
    override lateinit var responseStream: InputStream
    private val viewBag by lazy { mutableMapOf<String, Any?>() }

    override fun addHeaders(headers: Map<String, String>): Responsable {
        this.headers.putAll(headers)
        return this
    }

    override fun addHeader(key: String, value: String): Responsable {
        this.headers[key] = value
        return this
    }

    override fun addCookie(
        name: String,
        value: String?,
        lifetime: Duration,
        path: String?,
        domain: String?,
        secure: Boolean,
        httpOnly: Boolean
    ): Responsable {
        return addCookie(makeCookie(name, value, lifetime, path, domain, secure, httpOnly))
    }

    override fun addCookie(cookie: Cookie): Responsable {
        cookies.add(cookie)
        return this
    }

    override fun forgetCookie(name: String, path: String?, domain: String?): Responsable {
        return addCookie(name, "", lifetime = Duration.ZERO, path = path, domain = domain)
    }

    override fun finalize(call: HttpCall) {
        saveCookies()
        copyHeaders()
        copyContent(call)
    }

    private fun closeResponseStream() {
        if (::responseStream.isInitialized) {
            responseStream.close()
        }
    }

    private fun saveCookies() {
        cookies.forEach {
            servletResponse.addCookie(it)
        }
    }

    private fun copyContent(call: HttpCall) {
        if (::view.isInitialized) {
            try {
                call.make<ViewRenderer>().render(view, viewContext(call)).copyToResponse(servletResponse)
            } catch (e: Exception) {
                renderViewException(e, call)
            }
        } else if (::responseStream.isInitialized) {
            responseStream.copyToResponse(servletResponse)
        }
    }

    private fun renderViewException(e: Exception, call: HttpCall) {
        call.logger.error { e.printStackTrace() }
        val view = if (call.env.isDev) {
            View("errors/500", mapOf("stacktrace" to e.stackTraceString))
        } else {
            View("errors/500")
        }
        call.make<ViewRenderer>().render(view, viewContext(call)).copyToResponse(servletResponse)
    }

    private fun viewContext(call: HttpCall): MutableMap<String, Any?> {
        val flash = call.session.flashBag().toMutableMap()
        shareWithView("_csrf", call.session.csrfToken())
        shareWithView("call", call)
        shareWithView(OLD_INPUTS_KEY, flash.remove(OLD_INPUTS_KEY) ?: mapOf<String, List<Any>>())
        shareWithView("errors", flash.remove(VALIDATION_ERRORS_KEY) ?: mapOf<String, List<Any>>())
        if (!call.isDropped) {
            shareWithView("_route", call.route.target())
            shareWithView("_flash", flash)
            shareWithView("auth", call.authChannel)
        }

        return viewBag
    }

    private fun InputStream.copyToResponse(servletResponse: HttpServletResponse) {
        this.apply {
            copyTo(servletResponse.outputStream)
            closeResponseStream()
        }
    }

    private fun copyHeaders() {
        headers.forEach { (key, value) ->
            servletResponse.addHeader(key, value)
        }
    }

    private fun String.copyToResponse(servletResponse: HttpServletResponse) {
        byteInputStream(servletResponse.charset()).copyToResponse(servletResponse)
    }

    override fun shareWithView(key: String, value: Any?) {
        viewBag[key] = value
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
