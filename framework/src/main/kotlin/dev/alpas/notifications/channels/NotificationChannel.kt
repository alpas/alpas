package dev.alpas.notifications.channels

import dev.alpas.notifications.Notifiable
import dev.alpas.notifications.Notification

interface NotificationChannel {
    fun <T : Notifiable> send(notification: Notification<T>, notifiable: T)
}
