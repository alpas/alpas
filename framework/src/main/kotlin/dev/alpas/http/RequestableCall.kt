package dev.alpas.http

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import dev.alpas.JsonSerializer
import dev.alpas.cookie.CookieJar
import dev.alpas.ifNotBlank
import dev.alpas.isOneOf
import dev.alpas.session.Session
import java.nio.charset.Charset
import javax.servlet.http.HttpServletRequest
import org.eclipse.jetty.server.Request as JettyRequest

@Suppress("unused")
interface RequestableCall {
    val session: Session
    val acceptableContentTypes: List<String>
    val method: Method
    val uri: String
    val url: String
    val fullUri: String
    val remoteAddr: String
    val referrer: String?
    val rootUrl: String
    val fullUrl: String
    val queryString: String
    val isXmlHttpRequest: Boolean
    val isAjax: Boolean
    val isPjax: Boolean
    val isJson: Boolean
    val isPrefetch: Boolean
    val expectsJson: Boolean
    val wantsJson: Boolean
    val acceptsHtml: Boolean
    val acceptsAnyContentType: Boolean
    val isGet get() = methodIs(Method.GET)
    val isPost get() = methodIs(Method.POST)
    val isPut get() = methodIs(Method.PUT)
    val isPatch get() = methodIs(Method.PATCH)
    val isDelete get() = methodIs(Method.DELETE)
    val isReading get() = this.method.isReading()
    val cookie: CookieJar
    val jettyRequest: JettyRequest
    val body: String
    val jsonBody: Map<String, Any>?

    fun header(name: String): String?
    fun headers(name: String): List<String>?
    fun accepts(contentType: String, vararg contentTypes: String): Boolean {
        if (acceptsAnyContentType) {
            return true
        }

        acceptableContentTypes.forEach { accept ->
            return accept.isOneOf(contentType, *contentTypes)
        }

        return false
    }

    fun methodIs(method: Method) = this.method == method
}

class Requestable(private val servletRequest: HttpServletRequest) : RequestableCall {
    override val jettyRequest by lazy { servletRequest.getAttribute("jetty-request") as JettyRequest }
    override val cookie: CookieJar by lazy { CookieJar(servletRequest.cookies ?: emptyArray()) }
    override fun header(name: String): String? = servletRequest.getHeader(name)
    override fun headers(name: String): List<String>? = servletRequest.getHeaders(name).toList()
    override val method by lazy { Method.valueOf(servletRequest.method.toUpperCase()) }
    override val remoteAddr: String get() = servletRequest.remoteAddr
    override val referrer: String? get() = header("referer")
    override val queryString get() = servletRequest.queryString.ifNotBlank { query -> "?$query" }
    override val uri: String get() = servletRequest.requestURI
    override val fullUri get() = "$uri$queryString"
    override val url get() = servletRequest.requestURL.toString()
    override val fullUrl get() = servletRequest.requestURL.append(queryString).toString()
    override val rootUrl get() = jettyRequest.rootURL.toString()
    override val isXmlHttpRequest get() = header("X-Requested-With") == "XMLHttpRequest"
    override val isAjax get() = isXmlHttpRequest
    override val isPjax get() = header("X-PJAX") == "true"
    override val isPrefetch get() = header("X-Purpose") == "preview" || header("X-moz") == "prefetch"
    override val isJson get() = servletRequest.isJson
    override val expectsJson get() = (isAjax && !isPjax && acceptsAnyContentType) || wantsJson
    override val wantsJson get() = acceptableContentTypes.contains("application/json")
    override val acceptsHtml get() = accepts("text/html", "application/xhtml+xml")
    override val acceptsAnyContentType
        get() = acceptableContentTypes.isEmpty() || acceptableContentTypes.firstOrNull().isOneOf("*/*", "*")

    override val session by lazy { Session(servletRequest) }

    override val acceptableContentTypes: List<String> by lazy {
        header("Accept")?.split(',')?.map { it.trim() } ?: emptyList()
    }

    override val body: String by lazy {
        servletRequest.inputStream.readBytes().toString(Charset.forName(servletRequest.characterEncoding ?: "UTF-8"))
    }

    override val jsonBody by lazy {
        if (body.isBlank()) {
            mapOf<String, Any>()
        } else {
            try {
                JsonSerializer.deserialize<HashMap<String, Any>>(body)
            } catch (e: MismatchedInputException) {
                null
            } catch (ex: JsonParseException) {
                null
            }
        }
    }
}

internal val HttpServletRequest.isJson: Boolean
    get() {
        return getHeader("Content-Type")?.split(";")?.contains("application/json") ?: false
    }
