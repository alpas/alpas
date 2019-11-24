package dev.alpas.exceptions

import dev.alpas.http.HttpCall
import dev.alpas.session.csrfSessionKey
import dev.alpas.validation.ErrorBag
import org.eclipse.jetty.http.HttpStatus

class ValidationException(
    message: String? = null,
    headers: Map<String, String> = mapOf(),
    private val errorBag: ErrorBag
) : HttpException(HttpStatus.UNPROCESSABLE_ENTITY_422, message ?: "Validation Exception", headers = headers) {
    override fun report(call: HttpCall) {
        call.logger.warn { "Validation error: ${call.errorBag}" }
    }

    override fun render(call: HttpCall) {
        if (call.wantsJson) {
            call.reply(errorBag.asJson(), statusCode).asJson()
        } else {
            call.session.apply {
                flash("_validation_errors", errorBag.asMap())
                flash("_old_inputs", call.paramsExcept("password", "password_confirmation", csrfSessionKey))
            }
            call.redirect().back()
        }
    }
}
