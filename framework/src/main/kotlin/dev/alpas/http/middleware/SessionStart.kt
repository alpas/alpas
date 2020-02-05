package dev.alpas.http.middleware

import dev.alpas.Handler
import dev.alpas.Middleware
import dev.alpas.http.HttpCall
import dev.alpas.http.Method

class SessionStart : Middleware<HttpCall>() {
    override fun invoke(passable: HttpCall, forward: Handler<HttpCall>) {
        passable.authChannel.tryLogin()
        if (passable.methodIs(Method.GET) && !passable.isAjax && !passable.isPrefetch) {
            passable.session.savePreviousUrl(passable.fullUrl)
        }
        passable.session.copyPreviousFlashBag()
        forward(passable)
    }
}
