package dev.alpas.http

import dev.alpas.JsonSerializable
import dev.alpas.make
import dev.alpas.stackTraceString
import dev.alpas.toJson
import dev.alpas.view.ViewRenderer
import java.io.InputStream

interface Response {
    fun render(context: RenderContext, callback: InputStream.() -> Unit)

    fun renderException(exception: Exception, context: RenderContext, callback: InputStream.() -> Unit) {
    }
}

data class StringResponse(val payload: String?) : Response {
    override fun render(context: RenderContext, callback: InputStream.() -> Unit) {
        (payload ?: "").byteInputStream(context.call.charset()).use(callback)
    }
}

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

data class ViewResponse(val name: String, val args: Map<String, Any?>? = null) :
    Response {
    override fun render(context: RenderContext, callback: InputStream.() -> Unit) {
        val output = context.call.make<ViewRenderer>().render(context.copy(viewName = name), args)
        StringResponse(output).render(context, callback)
    }

    override fun renderException(exception: Exception, context: RenderContext, callback: InputStream.() -> Unit) {
        val call = context.call
        call.logger.error { exception.printStackTrace() }
        val view = if (call.env.isDev) {
            ViewResponse(
                "errors/500",
                mapOf("stacktrace" to exception.stackTraceString)
            )
        } else {
            ViewResponse("errors/500")
        }
        view.render(context, callback)
    }
}
