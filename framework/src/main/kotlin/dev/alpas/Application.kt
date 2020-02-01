package dev.alpas

import dev.alpas.http.HttpCall
import mu.KLogger
import java.io.File
import kotlin.reflect.KClass

interface Application : Container {
    val entryClass: Class<*>
    val logger: KLogger
    val srcPackage: String
    val env: Environment
    val configs: List<Config>
    val kernel: Kernel
    val cwd: File
    fun ignite()
    fun takeOff()
    fun stop()
    fun bufferDebugLog(log: String) {}
    fun <T : ServiceProvider> registerProvider(provider: T)
    fun recordLastCall(call: HttpCall) {}
    fun append(middleware: KClass<out Middleware<HttpCall>>, vararg others: KClass<out Middleware<HttpCall>>) {
        kernel.append(middleware, *others)
    }

    fun shouldLoadMiddleware(middleware: KClass<out Middleware<HttpCall>>): Boolean {
        return true
    }
}
