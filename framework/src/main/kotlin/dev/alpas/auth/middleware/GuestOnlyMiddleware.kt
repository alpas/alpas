package dev.alpas.auth.middleware

import dev.alpas.Handler
import dev.alpas.Middleware
import dev.alpas.config
import dev.alpas.auth.AuthConfig
import dev.alpas.http.HttpCall

class GuestOnlyMiddleware : Middleware<HttpCall>() {
    override fun invoke(call: HttpCall, forward: Handler<HttpCall>) {
        if (call.authChannel.isLoggedIn()) {
            call.redirect().to(call.config<AuthConfig>().ifAuthorizedRedirectToPath(call))
        } else {
            forward(call)
        }
    }
}
