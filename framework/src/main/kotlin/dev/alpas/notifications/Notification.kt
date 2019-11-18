package dev.alpas.notifications

import dev.alpas.mailing.MailMessage
import dev.alpas.notifications.channels.NotificationChannel
import java.io.Serializable
import kotlin.reflect.KClass

interface Notification<T : Notifiable> : MailableNotification<T>, Serializable {
    fun channels(): List<KClass<out NotificationChannel>>

    fun shouldQueue(notifiable: T): Boolean {
        return true
    }

    override fun toMail(notifiable: T): MailMessage {
        throw UnsupportedOperationException("Method not supported. If you need this method, override it.")
    }
}
