package dev.alpas.http

import dev.alpas.make
import dev.alpas.session.OLD_INPUTS_KEY
import dev.alpas.stackTraceString
import dev.alpas.view.ViewRenderer
import java.io.InputStream

data class ViewResponse(val name: String, val args: Map<String, Any?>? = null) :
    Response {
    override fun render(context: RenderContext, callback: InputStream.() -> Unit) {
        val call = context.call
        val viewData = buildViewData(call, args)
        val output = call.make<ViewRenderer>().renderContext(context.copy(viewName = name), viewData)
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

    private fun buildViewData(call: HttpCall, args: Map<String, Any?>?): Map<String, Any?> {
        val viewData = mutableMapOf(
            "_csrf" to call.session.csrfToken(),
            "call" to call,
            OLD_INPUTS_KEY to call.session.old(),
            "errors" to call.session.errors()

        )
        if (!call.isDropped) {
            viewData["_route"] = call.route.target()
            viewData["_flash"] = call.session.userFlashBag()
            viewData["auth"] = call.authChannel
        }
        if (args != null) {
            viewData.putAll(args)
        }
        return viewData
    }
}
