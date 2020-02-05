package dev.alpas.auth.middleware

import dev.alpas.Handler
import dev.alpas.Middleware
import dev.alpas.auth.AuthConfig
import dev.alpas.http.HttpCall
import dev.alpas.make

class GuestOnlyMiddleware : Middleware<HttpCall>() {
    override fun invoke(passable: HttpCall, forward: Handler<HttpCall>) {
        if (passable.authChannel.isLoggedIn()) {
            val config = passable.make<AuthConfig>()
            passable.redirect().to(config.ifAuthorizedRedirectToPath(passable))
        } else {
            forward(passable)
        }
    }
}
