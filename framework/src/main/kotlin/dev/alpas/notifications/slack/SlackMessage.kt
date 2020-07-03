package dev.alpas.notifications.slack

import dev.alpas.mustStartWith
import dev.alpas.toJson

open class SlackMessage {
    lateinit var content: String
    open var level: SlackMessageLevel = SlackMessageLevel.INFO
    var username: String? = null
    var icon: String? = null
    var image: String? = null
    var channel: String? = null
    var linkNames = false
    var unfurlLinks: String? = null
    var unfurlMedia: String? = null
    val attachments = mutableListOf<SlackAttachment>()

    fun asSuccess() = apply {
        level = SlackMessageLevel.SUCCESS
    }

    fun asError() = apply {
        level = SlackMessageLevel.ERROR
    }

    fun asWarning() = apply {
        level = SlackMessageLevel.WARNING
    }

    fun asInfo() = apply {
        level = SlackMessageLevel.INFO
    }

    open fun from(username: String, icon: String? = null) = apply {
        this.username = username
        this.icon = icon
    }

    open fun to(channel: String) = apply {
        this.channel = channel.mustStartWith("#")
    }

    open fun attachment(block: SlackAttachment.() -> Unit) = apply {
        SlackAttachment().also {
            block(it)
            attachments.add(it)
        }
    }

    open fun color(): String? {
        return when (level) {
            SlackMessageLevel.SUCCESS -> "good"
            SlackMessageLevel.WARNING -> "warning"
            SlackMessageLevel.ERROR -> "danger"
            else -> null
        }
    }

    open fun asJson(): String {
        val optionalFields = mapOf(
            "channel" to channel,
            "icon_emoji" to icon,
            "icon_url" to image,
            "link_names" to linkNames,
            "unfurl_links" to unfurlLinks,
            "unfurl_media" to unfurlMedia,
            "username" to username
        )

        return mapOf("text" to content, "attachments" to attachments(this)).plus(optionalFields).toJson()
    }

    protected open fun attachments(message: SlackMessage): List<Map<String, Any>> {
        return message.attachments.map {
            it.asMap(message)
        }
    }
}
