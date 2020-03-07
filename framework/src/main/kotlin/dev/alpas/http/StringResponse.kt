package dev.alpas.http

import org.eclipse.jetty.http.HttpStatus
import java.io.InputStream

open class StringResponse(
    val payload: String?,
    override var statusCode: Int = HttpStatus.OK_200,
    override var contentType: String = HTML_CONTENT_TYPE
) : Response {
    override fun render(context: RenderContext, callback: InputStream.() -> Unit) {
        (payload ?: "").byteInputStream(context.call.charset()).use(callback)
    }
}
