package dev.alpas.notifications

import dev.alpas.mailing.MailMessage

interface MailableNotification<T : Notifiable> : Notification<T> {
    fun toMail(notifiable: T): MailMessage
}
