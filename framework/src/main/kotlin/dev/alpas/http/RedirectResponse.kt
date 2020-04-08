package dev.alpas.http

import dev.alpas.Pipeline
import dev.alpas.cookie.CookieJar
import org.eclipse.jetty.http.HttpStatus

class RedirectResponse(
    private val location: String,
    var statusCode: Int = HttpStatus.MOVED_TEMPORARILY_302,
    val headers: Map<String, String> = emptyMap(),
    val cookie: CookieJar? = null,
    private val isExternal: Boolean = false
) : Response {

    private val flashes = mutableListOf<Pair<String, Any?>>()

    fun with(flashName: String, payload: Any?): RedirectResponse {
        flashes.add(flashName to payload)
        return this
    }

    override fun render(context: RenderContext) {
        flashes.forEach { (key, value) ->
            context.call.session.flash(key, value)
        }

        copyHeaders(context.call, headers)
        if (!isExternal) {
            saveCookies(context.call, cookie)
        }
        sendRedirect(context.call)
    }

    private fun sendRedirect(call: HttpCall) {
        Pipeline<RedirectResponse>().send(this).through(call.redirect().filters()).then { response ->
            (call.servletResponse as? org.eclipse.jetty.server.Response)?.sendRedirect(response.statusCode, response.location)
        }
    }

    private fun copyHeaders(call: HttpCall, headers: Map<String, String>) {
        headers.forEach { (key, value) ->
            call.servletResponse.addHeader(key, value)
        }
    }

    private fun saveCookies(call: HttpCall, cookie: CookieJar?) {
        cookie?.outgoingCookies?.forEach {
            call.servletResponse.addCookie(it)
        }
    }

    override fun statusCode(code: Int) {
        statusCode = code
    }
}
