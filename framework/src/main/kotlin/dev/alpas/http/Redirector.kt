package dev.alpas.http

import dev.alpas.Middleware
import dev.alpas.Pipeline
import dev.alpas.cookie.CookieJar
import dev.alpas.routing.UrlGenerator
import org.eclipse.jetty.http.HttpStatus
import org.eclipse.jetty.server.Response

open class RedirectFilter : Middleware<Redirect>()

interface Redirectable {
    fun isBeingRedirected(): Boolean

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
        params: Map<String, Any> = emptyMap(),
        status: Int = HttpStatus.MOVED_TEMPORARILY_302,
        headers: Map<String, String> = emptyMap()
    )

    fun pushFilter(filter: RedirectFilter)
}

class Redirector(
    private val request: RequestableCall,
    private val response: ResponsableCall,
    private val urlGenerator: UrlGenerator
) : Redirectable {

    lateinit var redirect: Redirect
        private set

    private val redirectFilters: MutableList<RedirectFilter> = mutableListOf()

    override fun isBeingRedirected() = ::redirect.isInitialized

    override fun to(to: String, status: Int, headers: Map<String, String>) {
        redirect = Redirect(to, status, headers, request.cookie)
    }

    override fun back(status: Int, headers: Map<String, String>, default: String) {
        to(url, status, headers)
        val url = request.referrer ?: request.session.pull("_previous_url", default)
    }

    override fun intended(default: String, status: Int, headers: Map<String, String>) {
        val url = request.session.pull("_intended_url", default)
        to(url, status, headers)
    }

    override fun toRouteNamed(name: String, params: Map<String, Any>, status: Int, headers: Map<String, String>) {
        to(urlGenerator.route(name, params = params), status, headers)
    }

    override fun home(status: Int, headers: Map<String, String>) {
        to(urlGenerator.route("home", defaultPath = "/"), status, headers)
    }

    override fun toExternal(url: String, status: Int, headers: Map<String, String>) {
        copyHeaders(headers)
        sendRedirect(Redirect(url, status, headers))
    }

    override fun pushFilter(filter: RedirectFilter) {
        redirectFilters.add(filter)
    }

    fun commitRedirect() {
        copyHeaders(redirect.headers)
        saveCookies(redirect.cookie)
        sendRedirect(redirect)
    }

    private fun sendRedirect(redirectObj: Redirect) {
        Pipeline<Redirect>().send(redirectObj).through(redirectFilters).then { finalRedirect ->
            (response.servletResponse as? Response)?.sendRedirect(finalRedirect.status, finalRedirect.location)
            request.jettyRequest.isHandled = true
        }
    }

    private fun copyHeaders(headers: Map<String, String>) {
        headers.forEach { (key, value) ->
            response.servletResponse.addHeader(key, value)
        }
    }

    private fun saveCookies(cookie: CookieJar?) {
        cookie?.outgoingCookies?.forEach {
            response.servletResponse.addCookie(it)
        }
    }
}

data class Redirect(
    val location: String,
    val status: Int,
    val headers: Map<String, String> = emptyMap(),
    val cookie: CookieJar? = null
)
