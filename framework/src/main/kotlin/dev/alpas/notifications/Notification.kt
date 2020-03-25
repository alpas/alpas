package dev.alpas.notifications

import dev.alpas.notifications.channels.NotificationChannel
import java.io.Serializable
import kotlin.reflect.KClass

interface Notification<T : Notifiable> : Serializable {
    fun channels(notifiable: T): List<KClass<out NotificationChannel>>

    fun shouldQueue(notifiable: T): Boolean {
        return false
    }

    fun onQueue(notifiable: T): String? = null
    fun queueConnection(notifiable: T): String? = null
}
