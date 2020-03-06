package dev.alpas.exceptions

import dev.alpas.http.HttpCall
import dev.alpas.session.CSRF_SESSION_KEY
import dev.alpas.session.OLD_INPUTS_KEY
import dev.alpas.session.VALIDATION_ERRORS_KEY
import dev.alpas.validation.ErrorBag
import org.eclipse.jetty.http.HttpStatus

class ValidationException(
    message: String? = null,
    headers: Map<String, String> = emptyMap(),
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
                flash(VALIDATION_ERRORS_KEY, errorBag.asMap())
                flash(
                    OLD_INPUTS_KEY,
                    call.paramsExcept(
                        "password",
                        "password_confirm",
                        "confirm_password",
                        CSRF_SESSION_KEY,
                        firstValueOnly = false
                    )
                )
            }
            call.redirect().back()
        }
    }
}
