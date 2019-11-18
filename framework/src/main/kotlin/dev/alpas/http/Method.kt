package dev.alpas.http

import dev.alpas.isOneOf

enum class Method {
    GET,
    HEAD,
    POST,
    PUT,
    DELETE,
    OPTIONS,
    TRACE,
    PATCH,
    UNKNOWN;

    fun isReading() = this.isOneOf(GET, HEAD, OPTIONS)
}
