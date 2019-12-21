package dev.alpas.mailing

import com.fasterxml.jackson.annotation.JsonIgnore
import dev.alpas.view.View
import dev.alpas.view.ViewRenderer

open class MailMessage {
    lateinit var to: String
    lateinit var subject: String
    lateinit var message: String

    @JsonIgnore
    protected var view: View? = null
    @JsonIgnore
    protected var viewBag: Map<String, Any?>? = null

    open fun view(templateName: String, args: Map<String, Any?>? = null): MailMessage {
        view = View(templateName.replace(".", "/"), args)
        viewBag = args
        return this
    }

    open fun render(viewRenderer: ViewRenderer) {
        if (view != null) {
            message = viewRenderer.render(view, viewBag ?: emptyMap())
        }
    }
}
