package dev.alpas.notifications.slack

data class SlackAttachmentField(val title: String, val content: Any? = null, val short: Boolean = true) {
    fun long() = copy(title = title, content = content, short = false)
    fun asMap() = mapOf("title" to title, "value" to content, "short" to short)
}
