package dev.alpas.mailing

import com.fasterxml.jackson.annotation.JsonIgnore

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

    open fun render(renderer: EmailRenderer = EmailRenderer(), extension: String = "peb") {
        if (view != null) {
            message = renderer.renderEmail(view!!, viewBag, extension)
        }
    }
}
