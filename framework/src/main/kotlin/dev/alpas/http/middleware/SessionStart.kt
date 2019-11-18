package dev.alpas.http.middleware

import dev.alpas.Handler
import dev.alpas.Middleware
import dev.alpas.http.HttpCall
import dev.alpas.http.Method

class SessionStart : Middleware<HttpCall>() {
    override fun invoke(call: HttpCall, forward: Handler<HttpCall>) {
        call.authChannel.tryLogin()
        if (call.methodIs(Method.GET) && !call.isAjax && !call.isPrefetch) {
            call.session.savePreviousUrl(call.fullUrl)
        }
        call.session.copyPreviousFlashBag()
        forward(call)
    }
}
