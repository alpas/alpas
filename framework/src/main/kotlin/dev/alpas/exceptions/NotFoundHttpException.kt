package dev.alpas.exceptions

import dev.alpas.http.HttpCall
import org.eclipse.jetty.http.HttpStatus

class NotFoundHttpException(message: String? = null, headers: Map<String, String> = mapOf()) :
    HttpException(HttpStatus.NOT_FOUND_404, message ?: "Not Found", headers = headers) {
    override fun report(call: HttpCall) {
        call.logger.warn { "Resource not found: ${call.fullUrl}" }
    }

    override fun render(call: HttpCall) {
        if (call.wantsJson) {
            call.reply(message, statusCode)
        } else {
            call.render("errors/404", statusCode = statusCode)
        }
    }
}
