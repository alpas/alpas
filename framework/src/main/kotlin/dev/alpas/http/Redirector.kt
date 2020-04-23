package dev.alpas.http

import dev.alpas.Middleware
import dev.alpas.routing.UrlGenerator
import org.eclipse.jetty.http.HttpStatus

open class RedirectFilter : Middleware<RedirectResponse>()

interface Redirectable {
    fun isBeingRedirected(): Boolean
    val redirectResponse: RedirectResponse

    fun to(
        to: String,
        status: Int = HttpStatus.MOVED_TEMPORARILY_302,
        headers: Map<String, String> = emptyMap()
    ): RedirectResponse

    fun toExternal(
        url: String,
        status: Int = HttpStatus.MOVED_TEMPORARILY_302,
        headers: Map<String, String> = emptyMap()
    ): RedirectResponse

    fun back(
        status: Int = HttpStatus.MOVED_TEMPORARILY_302,
        headers: Map<String, String> = emptyMap(),
        default: String = "/"
    ): RedirectResponse

    fun intended(
        default: String = "/",
        status: Int = HttpStatus.MOVED_TEMPORARILY_302,
        headers: Map<String, String> = emptyMap()
    ): RedirectResponse

    fun home(status: Int = HttpStatus.MOVED_TEMPORARILY_302, headers: Map<String, String> = emptyMap()): RedirectResponse
    fun toRouteNamed(
        name: String,
        params: Map<String, Any> = emptyMap(),
        status: Int = HttpStatus.MOVED_TEMPORARILY_302,
        headers: Map<String, String> = emptyMap()
    ): RedirectResponse

    fun pushFilter(filter: RedirectFilter)
    fun filters(): List<RedirectFilter>

    fun flash(name: String, payload: Any?)
}

class Redirector(
    private val request: RequestableCall,
    private val urlGenerator: UrlGenerator
) : Redirectable {
    override lateinit var redirectResponse: RedirectResponse
        private set

    private val redirectFilters: MutableList<RedirectFilter> = mutableListOf()

    override fun isBeingRedirected() = ::redirectResponse.isInitialized

    override fun to(to: String, status: Int, headers: Map<String, String>): RedirectResponse {
        redirectResponse = RedirectResponse(to, status, headers, request.cookie)
        return redirectResponse
    }

    override fun back(status: Int, headers: Map<String, String>, default: String): RedirectResponse {
        val url = request.referrer ?: request.session.pull("_previous_url", default)
        return to(url, status, headers)
    }

    override fun intended(default: String, status: Int, headers: Map<String, String>): RedirectResponse {
        val url = request.session.pull("_intended_url", default)
        return to(url, status, headers)
    }

    override fun toRouteNamed(name: String, params: Map<String, Any>, status: Int, headers: Map<String, String>): RedirectResponse {
        return to(urlGenerator.route(name, params = params), status, headers)
    }

    override fun home(status: Int, headers: Map<String, String>): RedirectResponse {
        return to(urlGenerator.route("home", defaultPath = "/"), status, headers)
    }

    override fun toExternal(url: String, status: Int, headers: Map<String, String>): RedirectResponse {
        redirectResponse = RedirectResponse(url, status, headers, isExternal = true)
        return redirectResponse
    }

    override fun pushFilter(filter: RedirectFilter) {
        redirectFilters.add(filter)
    }

    override fun filters(): List<RedirectFilter> {
        return redirectFilters.toList()
    }

    override fun flash(name: String, payload: Any?) {
        request.session.flash(name, payload)
    }
}
