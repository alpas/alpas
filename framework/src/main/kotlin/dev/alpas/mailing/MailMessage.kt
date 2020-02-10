package dev.alpas.mailing

import com.fasterxml.jackson.annotation.JsonIgnore
import dev.alpas.validation.SharedDataBag
import dev.alpas.view.ViewRenderer

open class MailMessage {
    lateinit var to: String
    lateinit var subject: String
    lateinit var message: String

    @JsonIgnore
    protected var view: String? = null
    @JsonIgnore
    protected var viewBag: Map<String, Any?>? = null

    open fun view(templateName: String, args: Map<String, Any?>? = null): MailMessage {
        view = templateName.replace(".", "/")
        viewBag = args
        return this
    }

    open fun render(viewRenderer: ViewRenderer) {
        if (view != null) {
            message = viewRenderer.renderView(view!!, SharedDataBag(), viewBag)
        }
    }
}
