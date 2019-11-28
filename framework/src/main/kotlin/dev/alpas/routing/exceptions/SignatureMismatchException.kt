package dev.alpas.routing.exceptions

import dev.alpas.exceptions.HttpException
import dev.alpas.http.HttpCall
import dev.alpas.stackTraceString
import org.eclipse.jetty.http.HttpStatus

open class SignatureMismatchException(
    message: String? = null,
    cause: Throwable? = null,
    headers: Map<String, String> = emptyMap()
) : HttpException(HttpStatus.FORBIDDEN_403, message ?: "Signature is invalid.", cause, headers) {
    override fun report(call: HttpCall) {
        call.logger.warn { this }
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
