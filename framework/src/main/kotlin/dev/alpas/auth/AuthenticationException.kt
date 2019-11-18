package dev.alpas.auth

import dev.alpas.config
import dev.alpas.exceptions.HttpException
import dev.alpas.http.HttpCall
import org.eclipse.jetty.http.HttpStatus

open class AuthenticationException(
    message: String? = null,
    cause: Throwable? = null,
    headers: Map<String, String> = mapOf()
) : HttpException(HttpStatus.UNAUTHORIZED_401, message ?: "Unauthenticated.", cause, headers) {
    override fun report(call: HttpCall) {
        call.logger.warn { "Unauthenticated" }
    }

    override fun render(call: HttpCall) {
        if (call.wantsJson) {
            call.reply(message, statusCode)
        } else {
            call.redirect().to(call.config<AuthConfig>().ifUnauthorizedRedirectToPath(call))
        }
    }
}
