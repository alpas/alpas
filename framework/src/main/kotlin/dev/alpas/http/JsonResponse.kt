package dev.alpas.http

import dev.alpas.JsonSerializable
import dev.alpas.toJson
import java.io.InputStream

data class JsonResponse(val payload: Any?) : Response {
    override fun render(context: RenderContext, callback: InputStream.() -> Unit) {
        val output = when (payload) {
            is JsonSerializable -> {
                payload.toJson()
            }
            is List<Any?> -> {
                mapOf("payload" to payload, "extras" to context.sharedDataBag.all()).toJson()
            }
            is Map<*, *> -> {
                payload.plus(context.sharedDataBag.all()).toJson()
            }
            else -> {
                payload?.toString()
            }
        }
        StringResponse(output).render(context, callback)
    }
}
