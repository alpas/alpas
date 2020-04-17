package dev.alpas.http

import dev.alpas.exceptions.HttpException
import dev.alpas.exceptions.ValidationException

open class ErrorResponse(val exception: Exception) : Response {
    override fun render(context: RenderContext) {
        handleException(context.call, exception)
        // The handleException method should have set a final response, either a string
        // kind or a redirect kind, In any case, we now need to close out the call.
        context.call.close()
    }

    private fun handleException(call: HttpCall, e: Throwable) {
        val exceptionHandler = call.exceptionHandler
        // Check if the top layer is an HTTP exception type since most of the times the exception is thrown from
        // a controller, the controller will be wrapped in an InvocationTargetException object. Hence, we'd have to
        // check the cause of this exception to see whether the cause is an actual HTTP exception or not.
        if (HttpException::class.java.isAssignableFrom(e::class.java)) {
            exceptionHandler.handle(e as HttpException, call)
            return
        } else {
            e.cause?.let {
                return handleException(call, it)
            }
        }
        exceptionHandler.handle(e, call)
        return
    }
}

class ValidationErrorResponse(val attribute: String, val message: String, val value: Any? = null) :
    ErrorResponse(ValidationException(RequestError(attribute, value = value, message = message)))
