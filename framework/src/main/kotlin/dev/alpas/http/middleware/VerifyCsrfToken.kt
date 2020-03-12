package dev.alpas.http.middleware

import dev.alpas.*
import dev.alpas.encryption.Encrypter
import dev.alpas.http.HttpCall
import dev.alpas.session.CSRF_SESSION_KEY
import dev.alpas.session.SessionConfig
import dev.alpas.session.TokenMismatchException

open class VerifyCsrfToken : Middleware<HttpCall>() {
    override fun invoke(passable: HttpCall, forward: Handler<HttpCall>) {
        if (passable.method.isReading() || isValidXSRFToken(passable)) {
            forward(passable)
            if (passable.sessionIsValid()) {
                addXSRFToken(passable)
            }
        } else {
            throw TokenMismatchException("Token doesn't match")
        }
    }

    private fun addXSRFToken(call: HttpCall) {
        val config = call.config<SessionConfig>()

        call.cookie.add(
            "XSRF-TOKEN",
            call.session.csrfToken() ?: "",
            lifetime = config.lifetime,
            path = config.path,
            domain = config.domain,
            secure = false,
            httpOnly = false
        )
    }

    private fun isValidXSRFToken(call: HttpCall): Boolean {
        val callToken = getCallToken(call)
        val sessionToken = call.session.csrfToken()
        if (callToken != null && sessionToken != null) {
            return hashEquals(callToken, sessionToken)
        }
        return false
    }

    private fun getCallToken(call: HttpCall): String? {
        val token = call.stringParam(CSRF_SESSION_KEY) ?: call.header("X-CSRF-TOKEN")
        val encryptedToken = call.header("X-XSRF-TOKEN")
        return if (token == null && encryptedToken != null) {
            return call.make<Encrypter>().decrypt(encryptedToken)
        } else {
            token
        }
    }
}
