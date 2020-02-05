package dev.alpas.http

import java.io.InputStream

data class StringResponse(val payload: String?) : Response {
    override fun render(context: RenderContext, callback: InputStream.() -> Unit) {
        (payload ?: "").byteInputStream(context.call.charset()).use(callback)
    }
}
