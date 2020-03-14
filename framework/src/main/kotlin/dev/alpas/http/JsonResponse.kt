package dev.alpas.http

import dev.alpas.JsonSerializable
import dev.alpas.toJson
import org.eclipse.jetty.http.HttpStatus
import java.io.InputStream

open class JsonResponse(
    val payload: Any?,
    override var statusCode: Int = HttpStatus.OK_200,
    override var contentType: String = JSON_CONTENT_TYPE
) : Response {

    override fun render(context: RenderContext, callback: InputStream.() -> Unit) {
        val output = when (payload) {
            is JsonSerializable, is Map<*, *> -> {
                payload.toJson()
            }
            is List<Any?> -> {
                mapOf("payload" to payload).toJson()
            }
            else -> {
                payload?.toString()
            }
        }
        StringResponse(output).render(context, callback)
    }
}

class AcknowledgementResponse(statusCode: Int = HttpStatus.NO_CONTENT_204) : JsonResponse("", statusCode)

