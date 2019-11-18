package dev.alpas.auth.middleware

import dev.alpas.Handler
import dev.alpas.Middleware
import dev.alpas.auth.AuthenticationException
import dev.alpas.http.HttpCall

class AuthOnlyMiddleware : Middleware<HttpCall>() {
    override fun invoke(call: HttpCall, forward: Handler<HttpCall>) {
        if (call.authChannel.check()) {
            forward(call)
        } else {
            call.session.saveIntendedUrl(call.fullUrl)
            throw AuthenticationException()
        }
    }
}
