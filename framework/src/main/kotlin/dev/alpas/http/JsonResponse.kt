package dev.alpas.http

import dev.alpas.JsonSerializable
import dev.alpas.JsonSerializer
import dev.alpas.exceptions.HttpException
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

class ErrorResponse(val exception: Exception) : Response {
    override fun render(context: RenderContext) {
        handleException(context.call, exception)
        // The handleException method should have set a final response, either a string
        // kind or a redirect kind, In any case, we now need to close out the call.
        context.call.close()
    }

    private fun handleException(call: HttpCall, e: Throwable) {
        val exceptionHandler = call.exceptionHandler
        // Check if the top layer is an HTTP exception type since most of the times the exception is thrown from
        // a controller, the controller will be wrapped in an InvocationTargetException object. Hence, we'd have to
        // check the cause of this exception to see whether the cause is an actual HTTP exception or not.
        if (HttpException::class.java.isAssignableFrom(e::class.java)) {
            exceptionHandler.handle(e as HttpException, call)
            return
        } else {
            e.cause?.let {
                return handleException(call, it)
            }
        }
        exceptionHandler.handle(e, call)
        return
    }
}
