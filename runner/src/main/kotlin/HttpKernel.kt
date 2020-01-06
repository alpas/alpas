package dev.alpas.runner

import dev.alpas.Application
import dev.alpas.ServiceProvider
import dev.alpas.http.HttpKernel
import dev.alpas.ozone.OzoneProvider
import kotlin.reflect.KClass

class HttpKernel : HttpKernel() {
    override fun serviceProviders(app: Application): Iterable<KClass<out ServiceProvider>> {
        return super.serviceProviders(app) + listOf(OzoneProvider::class)
    }
}
