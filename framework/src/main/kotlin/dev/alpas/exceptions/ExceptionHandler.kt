package dev.alpas.exceptions

import dev.alpas.http.HttpCall

open class ExceptionHandler {
    open fun report(exception: HttpException, call: HttpCall) {
        exception.report(call)
    }

    open fun render(exception: HttpException, call: HttpCall) {
        exception.render(call)
    }

    open fun handle(exception: HttpException, call: HttpCall) {
        report(exception, call)
        render(exception, call)
    }

    // User could override this method and decide if and how they want to handle a non-http exception.
    // If not we'd assume it is some internal server error and we'll report and render a catch-all
    // InternalServerError() exception. User shouldn't throw any exception from this handler but
    // rather call this super method and let it report and render the exception.
    open fun handle(exception: Throwable, call: HttpCall) {
        InternalServerException(exception.message, exception).apply {
            handle(this, call)
        }
    }
}
