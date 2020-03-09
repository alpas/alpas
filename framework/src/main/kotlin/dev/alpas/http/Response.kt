package dev.alpas.http

import java.io.InputStream
import javax.servlet.http.HttpServletResponse

internal val JSON_CONTENT_TYPE = "application/json; charset=UTF-8"
internal val HTML_CONTENT_TYPE = "text/html; charset=UTF-8"

interface Response {
    var statusCode: Int
    var contentType: String
    fun render(context: RenderContext, callback: InputStream.() -> Unit)
    fun renderException(exception: Exception, context: RenderContext, callback: InputStream.() -> Unit) {}
    fun finalize(response: HttpServletResponse) {
        response.status = statusCode
        response.contentType = contentType
    }
}
