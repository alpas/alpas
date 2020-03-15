package dev.alpas.http

import org.eclipse.jetty.http.HttpStatus

open class StringResponse(
    val payload: String?,
    var statusCode: Int = HttpStatus.OK_200,
    var contentType: String = HTML_CONTENT_TYPE
) : Response {
    override fun render(context: RenderContext) {
        val servletResponse = context.call.servletResponse
        servletResponse.status = statusCode
        servletResponse.contentType = contentType

        (payload ?: "").byteInputStream(context.call.charset()).use {
            it.copyTo(servletResponse.outputStream)
        }
    }

    override fun contentType(type: String) {
        contentType = type
    }

    override fun statusCode(code: Int) {
        statusCode = code
    }
}
