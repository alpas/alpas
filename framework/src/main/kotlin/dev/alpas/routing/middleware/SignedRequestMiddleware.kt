package dev.alpas.routing.middleware

import dev.alpas.Handler
import dev.alpas.Middleware
import dev.alpas.http.HttpCall
import dev.alpas.mustStartWith
import dev.alpas.routing.exceptions.SignatureMismatchException

class SignedRequestMiddleware : Middleware<HttpCall>() {
    override fun invoke(passable: HttpCall, forward: Handler<HttpCall>) {
        val url = passable.header("x-original-host")?.let {
            val scheme = passable.jettyRequest.scheme
            "$scheme://$it${passable.jettyRequest.originalURI}"
        }
        if (passable.isSigned(url)) {
            forward(passable)
        } else {
            throw SignatureMismatchException()
        }
    }
}
