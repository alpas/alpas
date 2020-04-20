package dev.alpas.mailing

import com.fasterxml.jackson.annotation.JsonIgnore

open class MailMessage {
    open lateinit var to: String
    open lateinit var subject: String
    open var message: String? = null
    open lateinit var extraProps: Map<String, Any?>

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
