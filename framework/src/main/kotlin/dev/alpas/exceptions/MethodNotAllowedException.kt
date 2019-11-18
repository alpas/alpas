package dev.alpas.exceptions

import org.eclipse.jetty.http.HttpStatus

// todo: accept and show allowed methods (only in dev mode?)
class MethodNotAllowedException(message: String? = null, headers: Map<String, String> = mapOf()) :
    HttpException(HttpStatus.METHOD_NOT_ALLOWED_405, message ?: "Method not allowed", headers = headers) {
}
