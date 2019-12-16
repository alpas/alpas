package dev.alpas.notifications

import dev.alpas.notifications.channels.NotificationChannel
import java.io.Serializable
import kotlin.reflect.KClass

interface Notification<T : Notifiable> : Serializable {
    fun channels(): List<KClass<out NotificationChannel>>

    fun shouldQueue(notifiable: T): Boolean {
        return true
    }
}
