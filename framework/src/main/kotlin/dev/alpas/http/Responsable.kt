package dev.alpas.http

import dev.alpas.validation.ErrorBag
import dev.alpas.validation.SharedDataBag
import javax.servlet.http.HttpServletResponse

class Responsable internal constructor(override val servletResponse: HttpServletResponse) :
    ResponsableCall {
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
        reply(call)
    }

    private fun saveCookies(call: HttpCall) {
        call.cookie.outgoingCookies.forEach {
            servletResponse.addCookie(it)
        }
    }

    private fun reply(call: HttpCall) {
        val context = RenderContext(call, sharedData).also {
            call.onBeforeRender(it)
        }
        try {
            response.render(context) { copyTo(servletResponse.outputStream) }
            response.finalize(servletResponse)
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

    override fun status(code: Int): ResponsableCall {
        if (::response.isInitialized) {
            response.statusCode = code
            return this
        } else {
            throw IllegalStateException("Status Code can't be set before the response. Make sure to set a response first.")
        }
    }


    override fun contentType(type: String): ResponsableCall {
        if (::response.isInitialized) {
            response.contentType = type
            return this
        } else {
            throw IllegalStateException("Content type can't be set before the response. Make sure to set a response first.")
        }
    }
}
