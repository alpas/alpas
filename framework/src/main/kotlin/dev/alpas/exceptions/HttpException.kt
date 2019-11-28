package dev.alpas.exceptions

import dev.alpas.http.HttpCall
import org.eclipse.jetty.http.HttpStatus

open class HttpException(
    val statusCode: Int,
    msg: String,
    cause: Throwable? = null,
    val headers: Map<String, String> = emptyMap()
) : RuntimeException(msg, cause) {
    open fun report(call: HttpCall) {}
    open fun render(call: HttpCall) {
        with(call) {
            addHeaders(headers)
            reply(message, statusCode)
            if (call.wantsJson) {
                call.asJson()
            }
        }
    }
}

internal fun Int.toHttpException(message: String? = null, headers: Map<String, String> = emptyMap()): HttpException {
    return when (this) {
        HttpStatus.NOT_FOUND_404 -> NotFoundHttpException(message, headers)
        HttpStatus.METHOD_NOT_ALLOWED_405 -> MethodNotAllowedException(message, headers = headers)
        HttpStatus.INTERNAL_SERVER_ERROR_500 -> InternalServerException(message, headers = headers)
        else -> HttpException(this, message ?: "", headers = headers)
    }
}
