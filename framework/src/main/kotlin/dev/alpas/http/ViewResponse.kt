package dev.alpas.http

import dev.alpas.make
import dev.alpas.session.CSRF_SESSION_KEY
import dev.alpas.session.OLD_INPUTS_KEY
import dev.alpas.stackTraceString
import dev.alpas.view.ViewRenderer
import org.eclipse.jetty.http.HttpStatus

open class ViewResponse(
    val name: String,
    val args: Map<String, Any?>? = null,
    var statusCode: Int = HttpStatus.OK_200,
    var contentType: String = HTML_CONTENT_TYPE
) : Response {
    override fun render(context: RenderContext) {
        val call = context.call
        val viewData = buildViewData(call, args)
        val output = call.make<ViewRenderer>().renderContext(context.copy(viewName = name), viewData)
        StringResponse(output, statusCode, contentType).render(context)
    }

    override fun renderException(exception: Exception, context: RenderContext) {
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
        view.render(context)
    }

    override fun contentType(type: String) {
        contentType = type
    }

    override fun statusCode(code: Int) {
        statusCode = code
    }

    private fun buildViewData(call: HttpCall, args: Map<String, Any?>?): Map<String, Any?> {
        val viewData = mutableMapOf(
            CSRF_SESSION_KEY to call.session.csrfToken(),
            "call" to call,
            OLD_INPUTS_KEY to call.session.old(),
            "errors" to call.session.errors()

        )
        if (!call.isDropped) {
            viewData["_route"] = call.route.target()
            viewData["_flash"] = call.session.userFlashBag()
            if (call.env.supportsSession) {
                viewData["auth"] = call.authChannel
            }
        }
        if (args != null) {
            viewData.putAll(args)
        }
        return viewData
    }
}
