package dev.alpas.http

import dev.alpas.JsonSerializable
import dev.alpas.JsonSerializer
import org.eclipse.jetty.http.HttpStatus

open class JsonResponse(
    val payload: Any = "{}",
    var statusCode: Int = HttpStatus.OK_200,
    private var contentType: String = JSON_CONTENT_TYPE
) : Response {
    override fun render(context: RenderContext) {
        val output = when (payload) {
            is JsonSerializable, is Map<*, *>, is List<Any?> -> {
                JsonSerializer.serialize(payload)
            }
            else -> {
                payload.toString()
            }
        }
        StringResponse(output, statusCode, contentType).render(context)
    }

    override fun contentType(type: String) {
        contentType = type
    }

    override fun statusCode(code: Int) {
        statusCode = code
    }
}

class AcknowledgementResponse(statusCode: Int = HttpStatus.NO_CONTENT_204) : JsonResponse(statusCode = statusCode) {
    override fun statusCode(code: Int) {
        statusCode = code
    }
}

open class EventStreamResponse : Response {
    override fun render(context: RenderContext) {
        context.call.servletResponse.apply {
            status = HttpStatus.OK_200
            contentType = "text/event-stream"
            addHeader("Connection", "close")
            addHeader("Cache-Control", "no-cache")
            flushBuffer()
        }
    }
}
