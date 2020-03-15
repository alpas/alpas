package dev.alpas.http

internal val JSON_CONTENT_TYPE = "application/json; charset=UTF-8"
internal val HTML_CONTENT_TYPE = "text/html; charset=UTF-8"

interface Response {
    fun render(context: RenderContext)
    fun renderException(exception: Exception, context: RenderContext) {}
    fun contentType(type: String) {}
    fun statusCode(code: Int) {}
}
