package dev.alpas.auth.middleware

import dev.alpas.Handler
import dev.alpas.Middleware
import dev.alpas.auth.AuthenticationException
import dev.alpas.http.HttpCall

class AuthOnlyMiddleware : Middleware<HttpCall>() {
    override fun invoke(passable: HttpCall, forward: Handler<HttpCall>) {
        if (passable.authChannel.check()) {
            forward(passable)
        } else {
            passable.session.saveIntendedUrl(passable.fullUrl)
            throw AuthenticationException()
        }
    }
}
