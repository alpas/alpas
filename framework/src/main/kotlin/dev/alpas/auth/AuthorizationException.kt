package dev.alpas.auth

import dev.alpas.config
import dev.alpas.exceptions.HttpException
import dev.alpas.http.HttpCall
import org.eclipse.jetty.http.HttpStatus

open class AuthorizationException(
    message: String? = null,
    cause: Throwable? = null,
    headers: Map<String, String> = emptyMap()
) : HttpException(HttpStatus.FORBIDDEN_403, message ?: "Unauthenticated.", cause, headers) {
    override fun report(call: HttpCall) {
        call.logger.warn { "Unauthorized" }
    }

    override fun render(call: HttpCall) {
        if (call.wantsJson) {
            call.reply(message, statusCode)
        } else {
            call.redirect().to(call.config<AuthConfig>().ifUnauthorizedRedirectToPath(call))
        }
    }
}
