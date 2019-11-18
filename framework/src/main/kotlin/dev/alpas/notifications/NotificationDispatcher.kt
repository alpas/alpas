package dev.alpas.notifications

import dev.alpas.Container
import dev.alpas.ephemeral
import dev.alpas.notifications.channels.NotificationChannel

class NotificationDispatcher(private val container: Container) {
    fun <T : Notifiable> dispatch(notification: Notification<T>, notifiables: List<T>) {
        notification.channels().forEach {
            val channel = container.ephemeral(it) as NotificationChannel
            channel.send(notification, notifiables)
        }
    }

    fun <T : Notifiable> dispatch(notification: Notification<T>, notifiable: T, vararg notifiables: T) {
        dispatch(notification, listOf(notifiable, *notifiables))
    }

    fun <T : Notifiable> dispatch(notification: Notification<T>, notifiable: T) {
        dispatch(notification, listOf(notifiable))
    }
}
