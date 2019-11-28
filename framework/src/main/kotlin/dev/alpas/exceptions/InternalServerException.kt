package dev.alpas.exceptions

import dev.alpas.http.HttpCall
import dev.alpas.stackTraceString
import org.eclipse.jetty.http.HttpStatus

class InternalServerException(
    message: String? = null,
    cause: Throwable? = null,
    headers: Map<String, String> = emptyMap()
) :
    HttpException(HttpStatus.INTERNAL_SERVER_ERROR_500, message ?: "Internal Server Error", cause, headers) {
    override fun report(call: HttpCall) {
        call.logger.error { stackTraceString }
    }

    override fun render(call: HttpCall) {
        when {
            call.wantsJson -> call.reply(message, statusCode)
            call.env.isDev -> // todo: render an exception page instead
                call.render("errors/500", mapOf("stacktrace" to stackTraceString), statusCode)
            else -> call.render("errors/500", statusCode = statusCode)
        }
    }
}
