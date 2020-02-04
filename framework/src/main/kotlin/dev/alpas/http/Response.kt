package dev.alpas.http

import java.io.InputStream

interface Response {
    fun render(context: RenderContext, callback: InputStream.() -> Unit)

    fun renderException(exception: Exception, context: RenderContext, callback: InputStream.() -> Unit) {
    }
}
