package dev.alpas.notifications.slack

import dev.alpas.notifications.Notifiable
import dev.alpas.notifications.Notification

interface SlackableNotification<T : Notifiable> : Notification<T> {
    fun toSlack(notifiable: T): SlackMessage
    fun webhook(message: SlackMessage, notifiable: T): String
}
