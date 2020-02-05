package dev.alpas.auth.middleware

import dev.alpas.Handler
import dev.alpas.Middleware
import dev.alpas.http.HttpCall

class VerifiedEmailOnlyMiddleware : Middleware<HttpCall>() {
    override fun invoke(passable: HttpCall, forward: Handler<HttpCall>) {
        if (!passable.isAuthenticated || (passable.user.mustVerifyEmail && !passable.user.isEmailVerified())) {
            passable.redirect().toRouteNamed("verification.notice")
        } else {
            forward(passable)
        }
    }
}

