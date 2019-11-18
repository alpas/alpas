package dev.alpas.auth.middleware

import dev.alpas.Handler
import dev.alpas.Middleware
import dev.alpas.http.HttpCall

class VerifiedEmailOnlyMiddleware : Middleware<HttpCall>() {
    override fun invoke(call: HttpCall, forward: Handler<HttpCall>) {
        if (!call.isAuthenticated || (call.user.mustVerifyEmail && !call.user.isEmailVerified())) {
            call.redirect().toRouteNamed("verification.notice")
        } else {
            forward(call)
        }
    }
}

