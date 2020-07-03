package dev.alpas.notifications.slack

data class SlackAction(val text: String, val url: String, val type: String = "button", val style: String = "")
