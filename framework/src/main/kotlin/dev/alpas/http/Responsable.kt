package dev.alpas.http

import dev.alpas.JsonSerializable
import dev.alpas.charset
import dev.alpas.exceptions.toHttpException
import dev.alpas.make
import dev.alpas.stackTraceString
import dev.alpas.validation.ErrorBag
import dev.alpas.view.View
import dev.alpas.view.ViewRenderer
import org.eclipse.jetty.http.HttpStatus
import java.io.InputStream
import javax.servlet.http.HttpServletResponse

interface Responsable {
    val errorBag: ErrorBag
    val servletResponse: HttpServletResponse
    var responseStream: InputStream
    var view: View

    fun addHeaders(headers: Map<String, String>)
    fun addHeader(key: String, value: String)

    fun <T> reply(payload: T? = null, statusCode: Int = HttpStatus.OK_200): Responsable {
        asHtml()
        responseStream = (payload?.toString() ?: "").byteInputStream(servletResponse.charset())
        servletResponse.status = statusCode
        return this
    }

    // todo: decide the default status code. 204 makes sense or not?
    fun acknowledge(statusCode: Int = HttpStatus.NO_CONTENT_204): Responsable {
        asJson()
        servletResponse.status = statusCode
        return this
    }

    fun send(obj: JsonSerializable, statusCode: Int = 200): Responsable {
        asJson()
        responseStream = obj.toJson().byteInputStream(servletResponse.charset())
        servletResponse.status = statusCode
        return this
    }

    fun asHtml() {
        servletResponse.contentType = "text/html; charset=UTF-8"
    }

    fun asJson() {
        servletResponse.contentType = "application/json; charset=UTF-8"
    }

    fun reply(view: View): Responsable {
        asHtml()
        this.view = view
        return this
    }

    private fun status(code: Int) {
        servletResponse.status = code
    }

    fun abort(statusCode: Int, message: String? = null, headers: Map<String, String> = emptyMap()): Nothing {
        throw statusCode.toHttpException(message, headers)
    }

    fun abortUnless(
        condition: Boolean,
        statusCode: Int,
        message: String? = null,
        headers: Map<String, String> = mapOf()
    ) {
        if (!condition) {
            abort(statusCode, message, headers)
        }
    }

    fun render(templateName: String, args: Map<String, Any?>? = null, statusCode: Int = HttpStatus.OK_200): View {
        status(statusCode)
        return View(templateName.replace(".", "/"), args).also {
            reply(it)
        }
    }

    fun shareWithView(key: String, value: Any?)

    // todo: this is confusing when calling call.send() from like a controller
    fun send(call: HttpCall)
}

class Response internal constructor(override val servletResponse: HttpServletResponse) : Responsable {

    private var headers: MutableMap<String, String> = mutableMapOf()
    override val errorBag: ErrorBag by lazy { ErrorBag() }
    override lateinit var view: View
    override lateinit var responseStream: InputStream
    private val viewBag by lazy { mutableMapOf<String, Any?>() }

    override fun addHeaders(headers: Map<String, String>) {
        this.headers.putAll(headers)
    }

    override fun addHeader(key: String, value: String) {
        this.headers[key] = value
    }

    override fun send(call: HttpCall) {
        saveCookies(call)
        copyHeaders()
        copyContent(call)
    }

    private fun closeResponseStream() {
        if (::responseStream.isInitialized) {
            responseStream.close()
        }
    }

    private fun saveCookies(call: HttpCall) {
        call.cookie.forEach {
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
        shareWithView("_old_inputs", flash.remove("_old_inputs") ?: mapOf<String, Any>())
        shareWithView("errors", flash.remove("_validation_errors") ?: mapOf<String, Any>())
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
