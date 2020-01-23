package dev.alpas.auth.middleware

import dev.alpas.Handler
import dev.alpas.Middleware
import dev.alpas.auth.AuthConfig
import dev.alpas.http.HttpCall
import dev.alpas.make

class GuestOnlyMiddleware : Middleware<HttpCall>() {
    override fun invoke(call: HttpCall, forward: Handler<HttpCall>) {
        if (call.authChannel.isLoggedIn()) {
            val config = call.make<AuthConfig>()
            call.redirect().to(config.ifAuthorizedRedirectToPath(call))
        } else {
            forward(call)
        }
    }
}
