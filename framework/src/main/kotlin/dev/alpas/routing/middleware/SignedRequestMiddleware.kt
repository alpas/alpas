package dev.alpas.routing.middleware

import dev.alpas.Handler
import dev.alpas.Middleware
import dev.alpas.http.HttpCall
import dev.alpas.routing.exceptions.SignatureMismatchException

class SignedRequestMiddleware : Middleware<HttpCall>() {
    override fun invoke(call: HttpCall, forward: Handler<HttpCall>) {
        if (call.isSigned()) {
            forward(call)
        } else {
            throw SignatureMismatchException()
        }
    }
}
