package dev.alpas.http

import dev.alpas.cookie.CookieJar
import dev.alpas.routing.UrlGenerator
import org.eclipse.jetty.http.HttpStatus
import org.eclipse.jetty.server.Response

interface Redirectable {
    val redirect: Redirect
    fun to(
        to: String,
        status: Int = HttpStatus.MOVED_TEMPORARILY_302,
        headers: Map<String, String> = emptyMap()
    )

    fun toExternal(
        url: String,
        status: Int = HttpStatus.MOVED_TEMPORARILY_302,
        headers: Map<String, String> = emptyMap()
    )

    fun back(
        status: Int = HttpStatus.MOVED_TEMPORARILY_302,
        headers: Map<String, String> = emptyMap(),
        default: String = "/"
    )

    fun intended(
        default: String = "/",
        status: Int = HttpStatus.MOVED_TEMPORARILY_302,
        headers: Map<String, String> = emptyMap()
    )

    fun home(status: Int = HttpStatus.MOVED_TEMPORARILY_302, headers: Map<String, String> = emptyMap())
    fun toRouteNamed(
        name: String,
        params: Map<String, Any>? = null,
        status: Int = HttpStatus.MOVED_TEMPORARILY_302,
        headers: Map<String, String> = emptyMap()
    )
}

class Redirector(
    private val request: Requestable,
    private val response: Responsable,
    private val urlGenerator: UrlGenerator
) : Redirectable {

    override lateinit var redirect: Redirect
        private set

    fun isDone(): Boolean {
        return ::redirect.isInitialized
    }

    override fun to(to: String, status: Int, headers: Map<String, String>) {
        commit(Redirect(to, status, headers, request.cookie))
    }

    override fun toExternal(url: String, status: Int, headers: Map<String, String>) {
        copyHeaders(headers)
        sendRedirect(Redirect(url, status, headers))
    }

    override fun back(status: Int, headers: Map<String, String>, default: String) {
        val url = request.header("referrer") ?: request.session.pull("_previous_url", default)
        to(url, status, headers)
    }

    override fun intended(default: String, status: Int, headers: Map<String, String>) {
        val url = request.session.pull("_intended_url", default)
        to(url, status, headers)
    }

    override fun home(status: Int, headers: Map<String, String>) {
        to(urlGenerator.route("home", defaultPath = "/"), status, headers)
    }

    private fun commit(redirectObj: Redirect) {
        copyHeaders(redirectObj.headers)
        saveCookies(redirectObj.cookie)
        sendRedirect(redirectObj)
    }

    private fun sendRedirect(redirectObj: Redirect) {
        redirect = redirectObj
        (response.servletResponse as? Response)?.sendRedirect(redirectObj.status, redirectObj.location)
        request.jettyRequest.isHandled = true
    }

    private fun copyHeaders(headers: Map<String, String>) {
        headers.forEach { (key, value) ->
            response.servletResponse.addHeader(key, value)
        }
    }

    private fun saveCookies(cookie: CookieJar?) {
        cookie?.forEach {
            response.servletResponse.addCookie(it)
        }
    }

    override fun toRouteNamed(name: String, params: Map<String, Any>?, status: Int, headers: Map<String, String>) {
        to(urlGenerator.route(name, params = params), status, headers)
    }
}

data class Redirect(
    val location: String,
    val status: Int,
    val headers: Map<String, String> = emptyMap(),
    val cookie: CookieJar? = null
)
