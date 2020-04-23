package dev.alpas.http

import dev.alpas.Config
import dev.alpas.Environment
import java.time.Duration

open class CorsConfig(env: Environment) : Config {
    open val allowedMethods = arrayOf("GET", "OPTIONS", "HEAD", "PUT", "POST", "PATCH", "DELETE")
    open val allowedOrigins = arrayOf("*")
    open val allowedHeaders = arrayOf("*")
    open val exposedHeaders: Array<String>? = null
    open val maxAge: Duration? = Duration.ofSeconds(60)
    open val supportsCredentials = false
}
