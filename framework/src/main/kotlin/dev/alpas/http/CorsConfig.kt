package dev.alpas.http

import dev.alpas.Config
import java.time.Duration

open class CorsConfig : Config {
    open val allowedMethods = arrayOf("GET", "OPTIONS", "HEAD", "PUT", "POST", "PATCH", "DELETE")
    open val allowedOrigins = arrayOf("*")
    open val allowedHeaders = arrayOf("*")
    open val exposedHeaders: Array<String>? = null
    open val maxAge: Duration? = Duration.ofSeconds(60)
    open val supportsCredentials = false
}
