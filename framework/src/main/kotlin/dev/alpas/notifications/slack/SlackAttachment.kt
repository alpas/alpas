package dev.alpas.notifications.slack

import dev.alpas.filterNotNullValues

class SlackAttachment {
    var fallback: String? = null
    var title: String? = null
    var url: String? = null
    var pretext: String? = null
    var content: String? = null
    var color: String? = null
    var fields = mutableListOf<SlackAttachmentField>()
    var markdown = emptyList<String>()
    var imageUrl: String? = null
    var thumbUrl: String? = null
    var authorName: String? = null
    var authorLink: String? = null
    var authorIcon: String? = null
    var footer: String? = null
    var footerIcon: String? = null
    val actions = mutableListOf<SlackAction>()

    fun title(title: String, url: String? = null) = apply {
        this.title = title
        this.url = url
    }

    fun field(title: String, content: String = "", block: SlackAttachmentField.() -> Unit) = apply {
        val field = SlackAttachmentField(title, content).also(block)
        fields.add(field)
    }

    fun action(title: String, url: String, style: String = "") = apply {
        actions.add(SlackAction(text = title, url = url, style = style))
    }

    fun author(name: String, link: String? = null, icon: String? = null) = apply {
        authorName = name
        authorLink = link
        authorIcon = icon
    }

    inline fun fields(builder: MutableList<SlackAttachmentField>.() -> Unit) {
        this.fields.addAll(mutableListOf<SlackAttachmentField>().also(builder))
    }

    fun asMap(message: SlackMessage): Map<String, Any> {
        return mapOf(
            "actions" to actions,
            "author_icon" to authorIcon,
            "author_link" to authorLink,
            "author_name" to authorName,
            "color" to (color ?: message.color()),
            "fallback" to fallback,
            "fields" to fields.map { it.asMap() },
            "footer" to footer,
            "footer_icon" to footerIcon,
            "image_url" to imageUrl,
            "mrkdown_in" to markdown,
            "pretext" to pretext,
            "text" to content,
            "thumb_url" to thumbUrl,
            "title" to title,
            "title_link" to url
            // todo: support timestamp
            //                "ts" to it.timestamp
        ).filterNotNullValues()
    }
}
