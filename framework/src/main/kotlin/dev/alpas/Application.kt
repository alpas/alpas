package dev.alpas

import dev.alpas.http.HttpCall
import dev.alpas.http.HttpCallHook
import mu.KLogger
import java.io.File
import java.net.URI
import kotlin.reflect.KClass

interface Application : Container {
    val entryClass: Class<*>
    val logger: KLogger
    val srcPackage: String
    val env: Environment
    val configs: List<Config>
    val kernel: Kernel
    val cwd: File
    val callHooks: Set<KClass<out HttpCallHook>>
    val appHooks: Set<AppHook>
    fun ignite()
    fun takeOff()
    fun stop()
    fun bufferDebugLog(log: String) {}
    fun <T : ServiceProvider> registerProvider(provider: T)
    fun recordLastCall(call: HttpCall) {}
    fun append(middleware: KClass<out Middleware<HttpCall>>, vararg others: KClass<out Middleware<HttpCall>>) {}

    fun shouldLoadMiddleware(middleware: KClass<out Middleware<HttpCall>>): Boolean {
        return true
    }

    fun <T : HttpCallHook> registerCallHook(hook: KClass<out T>, vararg hooks: KClass<out T>)
    fun <T : HttpCallHook> unregisterCallHook(hook: KClass<out T>)
    fun <T : AppHook> registerAppHook(hook: T, vararg hooks: T)
    fun <T : AppHook> unregisterAppHook(hook: T)

    fun inFlight(uri: URI)
}
