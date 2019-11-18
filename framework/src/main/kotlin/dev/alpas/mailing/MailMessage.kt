package dev.alpas.mailing

import com.fasterxml.jackson.annotation.JsonIgnore
import dev.alpas.view.View
import dev.alpas.view.ViewRenderer

open class MailMessage {
    lateinit var to: String
        protected set
    lateinit var subject: String
        protected set
    lateinit var text: String
        protected set

    @JsonIgnore
    protected var view: View? = null
    @JsonIgnore
    protected var viewBag: Map<String, Any?>? = null

    open fun toEmail(address: String): MailMessage {
        to = address
        return this
    }

    open fun subject(subject: String): MailMessage {
        this.subject = subject
        return this
    }

    open fun text(text: String): MailMessage {
        this.text = text
        return this
    }

    open fun view(templateName: String, args: Map<String, Any?>? = null): MailMessage {
        view = View(templateName.replace(".", "/"), args)
        viewBag = args
        return this
    }

    open fun render(viewRenderer: ViewRenderer) {
        if (view != null) {
            text = viewRenderer.render(view, viewBag ?: mapOf())
        }
    }
}
